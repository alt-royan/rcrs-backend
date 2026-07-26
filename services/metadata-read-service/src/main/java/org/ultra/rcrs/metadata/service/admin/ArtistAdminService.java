package org.ultra.rcrs.metadata.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.ArtistAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistAdminViewDto;
import org.ultra.rcrs.metadata.dto.PaginationResponse;
import org.ultra.rcrs.metadata.model.ArtistDocument;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistAdminService {

    private final ArtistDocumentRepository artistDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final S3Utils s3Utils;

    public Mono<ArtistAdminViewDto> getById(String id) {
        return artistDocumentRepository.findByIdForAdmin(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", id)))
                .map(this::toDto);
    }

    public Mono<PaginationResponse<ArtistAdminStandaloneDto>> getAll(String name,
                                                                     EntityStatus availabilityStatus,
                                                                     int offset,
                                                                     int limit) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Query filter = buildQuery(name, availabilityStatus);
        Query page = Query.of(filter).with(sort).skip(offset).limit(limit);

        Mono<List<ArtistAdminStandaloneDto>> items = mongoTemplate.find(page, ArtistDocument.class, "artists")
                .map(this::toStandaloneDto)
                .collectList();
        Mono<Long> totalCount = mongoTemplate.count(filter, ArtistDocument.class, "artists");

        return Mono.zip(items, totalCount,
                (found, total) -> new PaginationResponse<>(found, total, offset, limit));
    }

    private Query buildQuery(String name, EntityStatus availabilityStatus) {
        Query query = new Query();
        if (name != null && !name.isBlank()) {
            query.addCriteria(Criteria.where("name").regex(Pattern.quote(name), "i"));
        }
        if (availabilityStatus != null) {
            query.addCriteria(Criteria.where("availabilityStatus").is(availabilityStatus));
        }
        return query;
    }

    private ArtistAdminViewDto toDto(ArtistDocument doc) {
        return ArtistAdminViewDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                .avatarUrl(s3Utils.parseUrl(doc.getAvatarS3Key()))
                .socialLinks(doc.getSocialLinks() != null
                        ? doc.getSocialLinks().stream().map(s -> ArtistAdminViewDto.SocialLinkEmbed.builder()
                        .resourceName(s.getResourceName())
                        .url(s.getUrl())
                        .build()).collect(Collectors.toList())
                        : null)
                .tags(doc.getTags())
                .availabilityStatus(doc.getAvailabilityStatus())
                .build();
    }

    private ArtistAdminStandaloneDto toStandaloneDto(ArtistDocument doc) {
        return ArtistAdminStandaloneDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                .avatarUrl(s3Utils.parseUrl(doc.getAvatarS3Key()))
                .availabilityStatus(doc.getAvailabilityStatus())
                .build();
    }
}
