package org.ultra.rcrs.metadata.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.TrackAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.TrackAdminViewDto;
import org.ultra.rcrs.metadata.model.TrackDocument;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackAdminService {

    private final TrackDocumentRepository trackDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final S3Utils s3Utils;

    @Cacheable("tracks-admin")
    public Mono<TrackAdminViewDto> getById(String id) {
        return trackDocumentRepository.findByIdForAdmin(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Track", id)))
                .map(this::toDto);
    }

    @Cacheable("tracks-by-album-admin")
    public Flux<TrackAdminStandaloneDto> getAllByAlbumId(String albumId) {
        return trackDocumentRepository.findAllByAlbumIdForAdmin(albumId, Sort.by("trackNumber"))
                .map(this::toStandaloneDto);
    }

    public Flux<TrackAdminStandaloneDto> getAll(EntityStatus availabilityStatus,
                                                LifecycleStatus lifecycleStatus,
                                                String albumId,
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
        if (albumId != null) {
            query.addCriteria(Criteria.where("album.id").is(albumId));
        }
        if (explicit != null) {
            query.addCriteria(Criteria.where("explicit").is(explicit));
        }

        query.with(sort).skip(offset).limit(limit);
        return mongoTemplate.find(query, TrackDocument.class, "tracks")
                .map(this::toStandaloneDto);
    }

    public Mono<Long> count(EntityStatus availabilityStatus,
                            LifecycleStatus lifecycleStatus,
                            String albumId,
                            Boolean explicit) {
        Query query = new Query();

        if (availabilityStatus != null) {
            query.addCriteria(Criteria.where("availabilityStatus").is(availabilityStatus));
        }
        if (lifecycleStatus != null) {
            query.addCriteria(Criteria.where("lifecycleStatus").is(lifecycleStatus));
        }
        if (albumId != null) {
            query.addCriteria(Criteria.where("album.id").is(albumId));
        }
        if (explicit != null) {
            query.addCriteria(Criteria.where("explicit").is(explicit));
        }

        return mongoTemplate.count(query, TrackDocument.class, "tracks");
    }

    private TrackAdminViewDto toDto(TrackDocument doc) {
        return TrackAdminViewDto.builder()
                .id(doc.getId())
                .lifecycleStatus(doc.getLifecycleStatus())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .releaseDate(doc.getReleaseDate())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .album(doc.getAlbum() != null
                        ? TrackAdminViewDto.AlbumEmbed.builder()
                        .id(doc.getAlbum().getId())
                        .title(doc.getAlbum().getTitle())
                        .coverUrl(s3Utils.parseUrl(doc.getAlbum().getCoverS3Key()))
                        .build()
                        : null)
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackAdminViewDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .others(doc.getOthers() != null
                        ? doc.getOthers().stream().map(o -> TrackAdminViewDto.OtherArtistEmbed.builder()
                        .id(o.getId())
                        .name(o.getName())
                        .roles(o.getRoles())
                        .socialLinks(o.getSocialLinks() != null
                                ? o.getSocialLinks().stream().map(s -> TrackAdminViewDto.SocialLinkEmbed.builder()
                                .resourceName(s.getResourceName())
                                .url(s.getUrl())
                                .build()).collect(Collectors.toList())
                                : null)
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }

    private TrackAdminStandaloneDto toStandaloneDto(TrackDocument doc) {
        return TrackAdminStandaloneDto.builder()
                .id(doc.getId())
                .lifecycleStatus(doc.getLifecycleStatus())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackAdminStandaloneDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
