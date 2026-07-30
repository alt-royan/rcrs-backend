package org.ultra.rcrs.libraryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.libraryservice.dto.request.DownloadCollectionRequest;
import org.ultra.rcrs.libraryservice.dto.response.DownloadedCollectionDto;
import org.ultra.rcrs.libraryservice.dto.response.DownloadedTrackDto;
import org.ultra.rcrs.libraryservice.dto.response.PaginationResponse;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.Quality;
import org.ultra.rcrs.libraryservice.model.jpa.*;
import org.ultra.rcrs.libraryservice.repository.OffsetBasedPageRequest;
import org.ultra.rcrs.libraryservice.repository.jpa.DownloadedCollectionRepository;
import org.ultra.rcrs.libraryservice.repository.jpa.DownloadedCollectionTrackRepository;
import org.ultra.rcrs.libraryservice.repository.jpa.DownloadedTrackRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bookkeeping only. This service records what the user has on their device so the
 * library can render it; it never contacts media-service, issues no entitlement,
 * and imposes no expiry — a downloaded file stays until the user removes it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadService {

    private final DownloadedTrackRepository trackRepository;
    private final DownloadedCollectionRepository collectionRepository;
    private final DownloadedCollectionTrackRepository membershipRepository;

    public PaginationResponse<DownloadedTrackDto> listTracks(String userId, int offset, int limit) {
        var pageable = new OffsetBasedPageRequest(offset, limit, Sort.unsorted());
        List<DownloadedTrackDto> items = trackRepository.findByUserIdOrderByAddedAtDesc(userId, pageable).stream()
                .map(track -> new DownloadedTrackDto(
                        track.getTrackId(), track.getQuality(), track.isExplicit(), track.getAddedAt()))
                .toList();
        return new PaginationResponse<>(items, trackRepository.countByUserId(userId), offset, limit);
    }

    public PaginationResponse<DownloadedCollectionDto> listCollections(String userId, int offset, int limit) {
        var pageable = new OffsetBasedPageRequest(offset, limit, Sort.unsorted());
        List<DownloadedCollectionDto> items = collectionRepository.findByUserIdOrderByAddedAtDesc(userId, pageable).stream()
                .map(collection -> new DownloadedCollectionDto(
                        collection.getEntityType(), collection.getEntityId(), collection.getQuality(),
                        collection.getTrackCount(), collection.getAddedAt()))
                .toList();
        return new PaginationResponse<>(items, collectionRepository.countByUserId(userId), offset, limit);
    }

    /**
     * Records a single track download. Marks it explicit, so it survives the removal
     * of any collection that also happens to contain it.
     */
    @Transactional
    public void addTrack(String userId, String trackId, Quality quality) {
        upsertTrack(userId, trackId, quality, true);
    }

    @Transactional
    public void removeTrack(String userId, String trackId) {
        membershipRepository.deleteByTrack(userId, trackId);
        trackRepository.deleteById(new DownloadedTrackPK(userId, trackId));
    }

    /**
     * Records an album or playlist download. The client supplies the track ids
     * because this service holds no catalog data and makes no cross-service calls.
     */
    @Transactional
    public void addCollection(String userId, DownloadCollectionRequest request) {
        if (!request.getEntityType().isDownloadableCollection()) {
            throw new BadRequestException("only albums and playlists can be downloaded as a collection");
        }

        List<String> trackIds = request.getTrackIds().stream().distinct().toList();
        Instant now = Instant.now();

        collectionRepository.save(DownloadedCollection.builder()
                .userId(userId)
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .quality(request.getQuality())
                .trackCount(trackIds.size())
                .addedAt(now)
                .build());

        // Re-downloading a collection may drop tracks that are no longer part of it.
        membershipRepository.deleteMembership(userId, request.getEntityType(), request.getEntityId());
        membershipRepository.saveAll(trackIds.stream()
                .map(trackId -> DownloadedCollectionTrack.builder()
                        .userId(userId)
                        .entityType(request.getEntityType())
                        .entityId(request.getEntityId())
                        .trackId(trackId)
                        .build())
                .toList());

        trackIds.forEach(trackId -> upsertTrack(userId, trackId, request.getQuality(), false));

        // Tracks dropped from the re-downloaded collection may now be unreferenced.
        trackRepository.deleteOrphaned(userId);

        log.debug("Recorded download of {} {} ({} tracks) for user {}",
                request.getEntityType(), request.getEntityId(), trackIds.size(), userId);
    }

    /**
     * Removes a collection and the files it was the last claim on. Tracks the user
     * downloaded individually, or that another downloaded collection still lists,
     * stay on the device.
     */
    @Transactional
    public void removeCollection(String userId, LibraryEntityType type, String entityId) {
        membershipRepository.deleteMembership(userId, type, entityId);
        collectionRepository.deleteById(new DownloadedCollectionPK(userId, type, entityId));
        int orphaned = trackRepository.deleteOrphaned(userId);
        log.debug("Removed {} {} for user {}, dropping {} now-unreferenced tracks",
                type, entityId, userId, orphaned);
    }

    /**
     * Backs the download badges on a track list: the quality each track is held at,
     * or {@code null} when it is not downloaded.
     */
    public Map<String, Quality> check(String userId, Collection<String> trackIds) {
        Map<String, Quality> downloaded = new LinkedHashMap<>();
        trackRepository.findByUserIdAndTrackIdIn(userId, trackIds)
                .forEach(track -> downloaded.put(track.getTrackId(), track.getQuality()));

        Map<String, Quality> result = new LinkedHashMap<>();
        trackIds.forEach(trackId -> result.put(trackId, downloaded.get(trackId)));
        return result;
    }

    /**
     * There is one file per track on the device, so an existing row is updated
     * rather than duplicated. A track that was already downloaded explicitly stays
     * explicit even when a collection later includes it.
     */
    private void upsertTrack(String userId, String trackId, Quality quality, boolean explicit) {
        DownloadedTrack track = trackRepository.findById(new DownloadedTrackPK(userId, trackId))
                .orElseGet(() -> DownloadedTrack.builder()
                        .userId(userId)
                        .trackId(trackId)
                        .addedAt(Instant.now())
                        .build());
        track.setQuality(quality);
        track.setExplicit(track.isExplicit() || explicit);
        trackRepository.save(track);
    }
}
