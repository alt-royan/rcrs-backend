package org.ultra.rcrs.metadata.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.AlbumAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.AlbumAdminViewDto;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumAdminService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final S3Utils s3Utils;

    @Cacheable("albums-admin")
    public Mono<AlbumAdminViewDto> getById(String id) {
        return albumDocumentRepository.findByIdForAdmin(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Album", id)))
                .map(this::toDto);
    }

    public Flux<AlbumAdminStandaloneDto> getAllByArtistId(String artistId, AlbumType albumType, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "releaseDate");
        Query query = new Query(Criteria.where("artists.id").is(artistId));
        if (albumType != null) {
            query.addCriteria(Criteria.where("type").is(albumType));
        }
        query.with(sort);
        return mongoTemplate.find(query, AlbumPublicDocument.class, "albums")
                .map(this::toStandaloneDto);
    }

    public Flux<AlbumAdminStandaloneDto> getAll(EntityStatus availabilityStatus,
                                               LifecycleStatus lifecycleStatus,
                                               AlbumType type,
                                               Boolean explicit,
                                               int offset,
                                               int limit,
                                               String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "releaseDate");
        Query query = new Query();

        if (availabilityStatus != null) {
            query.addCriteria(Criteria.where("availabilityStatus").is(availabilityStatus));
        }
        if (lifecycleStatus != null) {
            query.addCriteria(Criteria.where("lifecycleStatus").is(lifecycleStatus));
        }
        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (explicit != null) {
            query.addCriteria(Criteria.where("explicit").is(explicit));
        }

        query.with(sort).skip(offset).limit(limit);
        return mongoTemplate.find(query, AlbumPublicDocument.class, "albums")
                .map(this::toStandaloneDto);
    }

    public Mono<Long> count(EntityStatus availabilityStatus,
                            LifecycleStatus lifecycleStatus,
                            AlbumType type,
                            Boolean explicit) {
        Query query = new Query();

        if (availabilityStatus != null) {
            query.addCriteria(Criteria.where("availabilityStatus").is(availabilityStatus));
        }
        if (lifecycleStatus != null) {
            query.addCriteria(Criteria.where("lifecycleStatus").is(lifecycleStatus));
        }
        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (explicit != null) {
            query.addCriteria(Criteria.where("explicit").is(explicit));
        }

        return mongoTemplate.count(query, AlbumPublicDocument.class, "albums");
    }

    private AlbumAdminViewDto toDto(AlbumPublicDocument doc) {
        return AlbumAdminViewDto.builder()
                .id(doc.getId())
                .lifecycleStatus(doc.getLifecycleStatus())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .type(doc.getType())
                .releaseDate(doc.getReleaseDate())
                .year(doc.getYear())
                .totalTracks(doc.getTotalTracks())
                .totalDurationMs(doc.getTotalDurationMs())
                .coverUrl(s3Utils.parseUrl(doc.getCoverS3Key()))
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> AlbumAdminViewDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }

    private AlbumAdminStandaloneDto toStandaloneDto(AlbumPublicDocument doc) {
        return AlbumAdminStandaloneDto.builder()
                .id(doc.getId())
                .lifecycleStatus(doc.getLifecycleStatus())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .type(doc.getType())
                .releaseDate(doc.getReleaseDate())
                .year(doc.getYear())
                .totalTracks(doc.getTotalTracks())
                .totalDurationMs(doc.getTotalDurationMs())
                .coverUrl(s3Utils.parseUrl(doc.getCoverS3Key()))
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> AlbumAdminStandaloneDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
