package org.ultra.rcrs.metadata.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.PaginationResponse;
import org.ultra.rcrs.metadata.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.TrackPublicViewDto;
import org.ultra.rcrs.metadata.model.TrackDocument;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;
import org.ultra.rcrs.utils.ImageUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackPublicService {

    private final TrackDocumentRepository trackDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final ImageUtils imageUtils;

    public Mono<TrackPublicViewDto> getById(String id) {
        return trackDocumentRepository.findByIdForPublic(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Track", id)))
                .map(this::toDto);
    }

    /**
     * Batch lookup for callers holding a list of track ids. Filtered to publicly
     * available tracks, like every other query behind the public controllers —
     * tracks that are unpublished or withdrawn are simply absent from the result.
     */
    public Mono<List<TrackPublicStandaloneDto>> getAllByIds(List<String> ids) {
        return trackDocumentRepository.findAllByIdInForPublic(ids)
                .map(this::toStandaloneDto)
                .collectList();
    }

    public Mono<PaginationResponse<TrackPublicStandaloneDto>> getAllByAlbumId(String albumId, int offset, int limit) {
        Sort sort = Sort.by("trackNumber");
        Query filter = new Query(Criteria.where("album.id").is(albumId)
                .and("lifecycleStatus").is("PUBLISHED")
                .and("availabilityStatus").in("ACTIVE", "HIDDEN"));
        Query page = Query.of(filter).with(sort).skip(offset).limit(limit);

        Mono<List<TrackPublicStandaloneDto>> items = mongoTemplate.find(page, TrackDocument.class, "tracks")
                .map(this::toStandaloneDto)
                .collectList();
        Mono<Long> totalCount = mongoTemplate.count(filter, TrackDocument.class, "tracks");

        return Mono.zip(items, totalCount,
                (found, total) -> new PaginationResponse<>(found, total, offset, limit));
    }

    private TrackPublicViewDto toDto(TrackDocument doc) {
        return TrackPublicViewDto.builder()
                .id(doc.getId())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .releaseDate(doc.getReleaseDate())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .album(doc.getAlbum() != null
                        ? TrackPublicViewDto.AlbumEmbed.builder()
                        .id(doc.getAlbum().getId())
                        .title(doc.getAlbum().getTitle())
                        .cover(imageUtils.parseUrls(doc.getAlbum().getCoverS3Key()))
                        .build()
                        : null)
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackPublicViewDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatar(imageUtils.parseUrls(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .others(doc.getOthers() != null
                        ? doc.getOthers().stream().map(o -> TrackPublicViewDto.OtherArtistEmbed.builder()
                        .id(o.getId())
                        .name(o.getName())
                        .roles(o.getRoles())
                        .socialLinks(o.getSocialLinks() != null
                                ? o.getSocialLinks().stream().map(s -> TrackPublicViewDto.SocialLinkEmbed.builder()
                                .resourceName(s.getResourceName())
                                .url(s.getUrl())
                                .build()).collect(Collectors.toList())
                                : null)
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }

    private TrackPublicStandaloneDto toStandaloneDto(TrackDocument doc) {
        return TrackPublicStandaloneDto.builder()
                .id(doc.getId())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .album(TrackPublicStandaloneDto.AlbumEmbed.builder()
                        .id(doc.getAlbum().getId())
                        .title(doc.getAlbum().getTitle())
                        .cover(imageUtils.parseUrls(doc.getAlbum().getCoverS3Key()))
                        .build())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackPublicStandaloneDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatar(imageUtils.parseUrls(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
