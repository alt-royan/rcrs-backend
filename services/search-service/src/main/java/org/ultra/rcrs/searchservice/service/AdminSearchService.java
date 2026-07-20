package org.ultra.rcrs.searchservice.service;

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.document.AlbumAdminDoc;
import org.ultra.rcrs.searchservice.document.ArtistAdminDoc;
import org.ultra.rcrs.searchservice.document.TrackAdminDoc;
import org.ultra.rcrs.searchservice.dto.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchCollection<ArtistResultWrapper> searchArtists(String query, int page, int size) {
        var nativeQ = NativeQuery.builder()
                .withQuery(Query.of(q -> q
                        .bool(b -> b
                                .should(s -> s.multiMatch(mm -> mm
                                        .query(query)
                                        .fields("name^3", "tags^1.5")
                                        .type(TextQueryType.BestFields)
                                ))
                                .should(s -> s.nested(n -> n
                                        .path("tracks")
                                        .query(nq -> nq.multiMatch(mm -> mm
                                                .query(query)
                                                .fields("tracks.title^1")
                                                .type(TextQueryType.BestFields)
                                        ))
                                        .scoreMode(ChildScoreMode.Sum)
                                ))
                                .should(s -> s.nested(n -> n
                                        .path("albums")
                                        .query(nq -> nq.multiMatch(mm -> mm
                                                .query(query)
                                                .fields("albums.title^1")
                                                .type(TextQueryType.BestFields)
                                        ))
                                        .scoreMode(ChildScoreMode.Sum)
                                ))
                        )
                ))
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<ArtistAdminDoc> hits = elasticsearchOperations.search(nativeQ, ArtistAdminDoc.class);

        var list = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toArtistResult)
                .map(ArtistResultWrapper::new)
                .toList();

        return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
    }

    public SearchCollection<AlbumResultWrapper> searchAlbums(String query, int page, int size) {
        var nativeQ = NativeQuery.builder()
                .withQuery(Query.of(q -> q
                        .bool(b -> b
                                .should(s -> s.multiMatch(mm -> mm
                                        .query(query)
                                        .fields("title^3", "year^1.5")
                                        .type(TextQueryType.BestFields)
                                ))
                                .should(s -> s.nested(n -> n
                                        .path("tracks")
                                        .query(nq -> nq.multiMatch(mm -> mm
                                                .query(query)
                                                .fields("tracks.title^1")
                                                .type(TextQueryType.BestFields)
                                        ))
                                        .scoreMode(ChildScoreMode.Sum)
                                ))
                                .should(s -> s.nested(n -> n
                                        .path("artists")
                                        .query(nq -> nq.multiMatch(mm -> mm
                                                .query(query)
                                                .fields("artists.name^1")
                                                .type(TextQueryType.BestFields)
                                        ))
                                        .scoreMode(ChildScoreMode.Sum)
                                ))
                        )
                ))
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<AlbumAdminDoc> hits = elasticsearchOperations.search(nativeQ, AlbumAdminDoc.class);

        var list = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toAlbumResult)
                .map(AlbumResultWrapper::new)
                .toList();

        return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
    }

    public SearchCollection<TrackResultWrapper> searchTracks(String query, int page, int size) {
        var nativeQ = NativeQuery.builder()
                .withQuery(Query.of(q -> q
                        .bool(b -> b
                                .should(s -> s.multiMatch(mm -> mm
                                        .query(query)
                                        .fields("title^3")
                                        .type(TextQueryType.BestFields)
                                ))
                                .should(s -> s.nested(n -> n
                                        .path("artists")
                                        .query(nq -> nq.multiMatch(mm -> mm
                                                .query(query)
                                                .fields("artists.name^1.5")
                                                .type(TextQueryType.BestFields)
                                        ))
                                        .scoreMode(ChildScoreMode.Sum)
                                ))
                                .should(s -> s.nested(n -> n
                                        .path("album")
                                        .query(nq -> nq.multiMatch(mm -> mm
                                                .query(query)
                                                .fields("album.title^1.5")
                                                .type(TextQueryType.BestFields)
                                        ))
                                        .scoreMode(ChildScoreMode.Max)
                                ))
                        )
                ))
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<TrackAdminDoc> hits = elasticsearchOperations.search(nativeQ, TrackAdminDoc.class);

        var list = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toTrackResult)
                .map(TrackResultWrapper::new)
                .toList();

        return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
    }

    private ArtistSearchResult toArtistResult(ArtistAdminDoc doc) {
        var albums = doc.getAlbums() != null
                ? doc.getAlbums().stream().map(a -> new ArtistSearchResult.NestedAlbumDto(a.getId(), a.getTitle())).toList()
                : Collections.<ArtistSearchResult.NestedAlbumDto>emptyList();
        var tracks = doc.getTracks() != null
                ? doc.getTracks().stream().map(t -> new ArtistSearchResult.NestedTrackDto(t.getId(), t.getTitle())).toList()
                : Collections.<ArtistSearchResult.NestedTrackDto>emptyList();
        return new ArtistSearchResult(doc.getId(), doc.getName(), doc.getTags(),
                doc.getAvailability() != null ? doc.getAvailability().name() : null, albums, tracks);
    }

    private AlbumSearchResult toAlbumResult(AlbumAdminDoc doc) {
        var artists = doc.getArtists() != null
                ? doc.getArtists().stream().map(a -> new AlbumSearchResult.NestedArtistDto(a.getId(), a.getName())).toList()
                : Collections.<AlbumSearchResult.NestedArtistDto>emptyList();
        var tracks = doc.getTracks() != null
                ? doc.getTracks().stream().map(t -> new AlbumSearchResult.NestedTrackDto(t.getId(), t.getTitle())).toList()
                : Collections.<AlbumSearchResult.NestedTrackDto>emptyList();
        return new AlbumSearchResult(doc.getId(), doc.getTitle(), doc.getYear(),
                doc.getAvailability() != null ? doc.getAvailability().name() : null,
                doc.getLifecycleStatus() != null ? doc.getLifecycleStatus().name() : null,
                artists, tracks);
    }

    private TrackSearchResult toTrackResult(TrackAdminDoc doc) {
        var artists = doc.getArtists() != null
                ? doc.getArtists().stream().map(a -> new TrackSearchResult.NestedArtistDto(a.getId(), a.getName())).toList()
                : Collections.<TrackSearchResult.NestedArtistDto>emptyList();
        TrackSearchResult.NestedAlbumDto albumDto = null;
        if (doc.getAlbum() != null) {
            albumDto = new TrackSearchResult.NestedAlbumDto(doc.getAlbum().getId(), doc.getAlbum().getTitle());
        }
        return new TrackSearchResult(doc.getId(), doc.getTitle(),
                doc.getAvailability() != null ? doc.getAvailability().name() : null,
                doc.getLifecycleStatus() != null ? doc.getLifecycleStatus().name() : null,
                artists, albumDto);
    }
}
