package org.ultra.rcrs.libraryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.libraryservice.dto.request.LikeCheckRequest;
import org.ultra.rcrs.libraryservice.dto.response.LikeSummaryResponse;
import org.ultra.rcrs.libraryservice.dto.response.LikedEntityDto;
import org.ultra.rcrs.libraryservice.dto.response.PaginationResponse;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.jpa.Like;
import org.ultra.rcrs.libraryservice.model.jpa.LikePK;
import org.ultra.rcrs.libraryservice.repository.OffsetBasedPageRequest;
import org.ultra.rcrs.libraryservice.repository.jpa.LikeRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    public PaginationResponse<LikedEntityDto> list(String userId, LibraryEntityType type, int offset, int limit) {
        var pageable = new OffsetBasedPageRequest(offset, limit, Sort.by(Sort.Direction.DESC, "likedAt"));
        List<LikedEntityDto> items = likeRepository.findByUserIdAndEntityType(userId, type, pageable).stream()
                .map(like -> new LikedEntityDto(like.getEntityType(), like.getEntityId(), like.getLikedAt()))
                .toList();
        long total = likeRepository.countByUserIdAndEntityType(userId, type);
        return new PaginationResponse<>(items, total, offset, limit);
    }

    /**
     * Idempotent: liking something already liked leaves the original timestamp
     * alone, so a retried request does not reorder the user's library.
     */
    @Transactional
    public void like(String userId, LibraryEntityType type, String entityId) {
        LikePK key = new LikePK(userId, type, entityId);
        if (likeRepository.existsById(key)) {
            return;
        }
        likeRepository.save(Like.builder()
                .userId(userId)
                .entityType(type)
                .entityId(entityId)
                .likedAt(Instant.now())
                .build());
        log.debug("Liked {} {} for user {}", type, entityId, userId);
    }

    /** Idempotent: unliking something that is not liked is a no-op. */
    @Transactional
    public void unlike(String userId, LibraryEntityType type, String entityId) {
        likeRepository.deleteById(new LikePK(userId, type, entityId));
    }

    /**
     * Answers a whole screen's worth of heart icons in one call. Items are grouped
     * by type so the cost is one indexed query per distinct type present — at most
     * four — instead of one per item.
     *
     * @return keys of the form {@code TYPE:id}, mapped to whether the caller likes them
     */
    public Map<String, Boolean> check(String userId, LikeCheckRequest request) {
        Map<LibraryEntityType, List<String>> byType = request.getItems().stream()
                .collect(Collectors.groupingBy(LikeCheckRequest.Item::getType,
                        Collectors.mapping(LikeCheckRequest.Item::getId, Collectors.toList())));

        Map<String, Boolean> result = new LinkedHashMap<>();
        byType.forEach((type, ids) -> {
            Set<String> liked = new HashSet<>(likeRepository.findLikedEntityIds(userId, type, ids));
            ids.forEach(id -> result.put(key(type, id), liked.contains(id)));
        });
        return result;
    }

    public LikeSummaryResponse summary(String userId) {
        Map<LibraryEntityType, Long> counts = new EnumMap<>(LibraryEntityType.class);
        for (Object[] row : likeRepository.countByTypeForUser(userId)) {
            counts.put((LibraryEntityType) row[0], (Long) row[1]);
        }
        return new LikeSummaryResponse(
                counts.getOrDefault(LibraryEntityType.TRACK, 0L),
                counts.getOrDefault(LibraryEntityType.ALBUM, 0L),
                counts.getOrDefault(LibraryEntityType.ARTIST, 0L),
                counts.getOrDefault(LibraryEntityType.PLAYLIST, 0L));
    }

    private static String key(LibraryEntityType type, String id) {
        return type.name() + ":" + id;
    }
}
