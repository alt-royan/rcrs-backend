package org.ultra.rcrs.metadata.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.AlbumPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.AlbumPublicViewDto;
import org.ultra.rcrs.metadata.dto.PaginationResponse;
import org.ultra.rcrs.metadata.model.AlbumDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.metadata.repository.AlbumTotalsRepository;
import org.ultra.rcrs.metadata.repository.AlbumTotalsRepository.AlbumTotals;
import org.ultra.rcrs.utils.ImageUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static org.ultra.rcrs.metadata.repository.AlbumTotalsRepository.totalsOf;

@Service
@RequiredArgsConstructor
public class AlbumPublicService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final AlbumTotalsRepository albumTotalsRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final ImageUtils imageUtils;

    public Mono<AlbumPublicViewDto> getById(String id) {
        return albumDocumentRepository.findByIdForPublic(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Album", id)))
                .flatMap(doc -> albumTotalsRepository.findTotalsForPublic(List.of(doc.getId()))
                        .map(totals -> toDto(doc, totalsOf(totals, doc.getId()))));
    }

    public Mono<PaginationResponse<AlbumPublicStandaloneDto>> getAllByArtistId(String artistId, AlbumType albumType, String sortDirection, int offset, int limit) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "releaseDate");
        Query filter = new Query(Criteria.where("artists.id").is(artistId)
                .and("lifecycleStatus").is("PUBLISHED")
                .and("availabilityStatus").in("ACTIVE", "HIDDEN"));
        if (albumType != null) {
            filter.addCriteria(Criteria.where("type").is(albumType));
        }
        Query page = Query.of(filter).with(sort).skip(offset).limit(limit);

        Mono<List<AlbumPublicStandaloneDto>> items = mongoTemplate.find(page, AlbumDocument.class, "albums")
                .collectList()
                .flatMapMany(this::toStandaloneDtos)
                .collectList();
        Mono<Long> totalCount = mongoTemplate.count(filter, AlbumDocument.class, "albums");

        return Mono.zip(items, totalCount,
                (found, total) -> new PaginationResponse<>(found, total, offset, limit));
    }

    private Flux<AlbumPublicStandaloneDto> toStandaloneDtos(List<AlbumDocument> docs) {
        List<String> ids = docs.stream().map(AlbumDocument::getId).toList();
        return albumTotalsRepository.findTotalsForPublic(ids)
                .flatMapMany(totals -> Flux.fromIterable(docs)
                        .map(doc -> toStandaloneDto(doc, totalsOf(totals, doc.getId()))));
    }

    private AlbumPublicViewDto toDto(AlbumDocument doc, AlbumTotals totals) {
        return AlbumPublicViewDto.builder()
                .id(doc.getId())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .type(doc.getType())
                .releaseDate(doc.getReleaseDate())
                .year(doc.getYear())
                .totalTracks(totals.totalTracks())
                .totalDurationMs(totals.totalDurationMs())
                .coverUrl(imageUtils.parseUrl(doc.getCoverS3Key()))
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> AlbumPublicViewDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(imageUtils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }

    private AlbumPublicStandaloneDto toStandaloneDto(AlbumDocument doc, AlbumTotals totals) {
        return AlbumPublicStandaloneDto.builder()
                .id(doc.getId())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .type(doc.getType())
                .releaseDate(doc.getReleaseDate())
                .year(doc.getYear())
                .totalTracks(totals.totalTracks())
                .totalDurationMs(totals.totalDurationMs())
                .coverUrl(imageUtils.parseUrl(doc.getCoverS3Key()))
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> AlbumPublicStandaloneDto.ArtistEmbed.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(imageUtils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole())
                        .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
