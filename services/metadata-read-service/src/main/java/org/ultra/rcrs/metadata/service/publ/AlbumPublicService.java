package org.ultra.rcrs.metadata.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.metadata.dto.AlbumPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.AlbumPublicViewDto;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumPublicService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final S3Utils s3Utils;

    @Cacheable("albums-public")
    public Mono<AlbumPublicViewDto> getById(String id) {
        return albumDocumentRepository.findByIdForPublic(id)
                .map(this::toDto);
    }

    public Flux<AlbumPublicStandaloneDto> getAllByArtistId(String artistId, AlbumType albumType, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "releaseDate");
        Query query = new Query(Criteria.where("artists.id").is(artistId)
                .and("lifecycleStatus").is("PUBLISHED")
                .and("availabilityStatus").in("ACTIVE", "HIDDEN"));
        if (albumType != null) {
            query.addCriteria(Criteria.where("type").is(albumType));
        }
        query.with(sort);
        return mongoTemplate.find(query, AlbumPublicDocument.class, "albums")
                .map(this::toStandaloneDto);
    }

    private AlbumPublicViewDto toDto(AlbumPublicDocument doc) {
        return AlbumPublicViewDto.builder()
                .id(doc.getId())
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
                        ? doc.getArtists().stream().map(a -> AlbumPublicViewDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }

    private AlbumPublicStandaloneDto toStandaloneDto(AlbumPublicDocument doc) {
        return AlbumPublicStandaloneDto.builder()
                .id(doc.getId())
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
                        ? doc.getArtists().stream().map(a -> AlbumPublicStandaloneDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
