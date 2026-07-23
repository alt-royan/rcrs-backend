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
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistAdminService {

    private final ArtistDocumentRepository artistDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final S3Utils s3Utils;

    @Cacheable("artists-admin")
    public Mono<ArtistAdminViewDto> getById(String id) {
        return artistDocumentRepository.findByIdForAdmin(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", id)))
                .map(this::toDto);
    }

    public Flux<ArtistAdminStandaloneDto> getAll(EntityStatus availabilityStatus, int offset, int limit) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Query query = new Query();

        if (availabilityStatus != null) {
            query.addCriteria(Criteria.where("availabilityStatus").is(availabilityStatus));
        }

        query.with(sort).skip(offset).limit(limit);
        return mongoTemplate.find(query, ArtistPublicDocument.class, "artists")
                .map(this::toStandaloneDto);
    }

    public Mono<Long> count(EntityStatus availabilityStatus) {
        Query query = new Query();
        if (availabilityStatus != null) {
            query.addCriteria(Criteria.where("availabilityStatus").is(availabilityStatus));
        }
        return mongoTemplate.count(query, ArtistPublicDocument.class, "artists");
    }

    private ArtistAdminViewDto toDto(ArtistPublicDocument doc) {
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
}
