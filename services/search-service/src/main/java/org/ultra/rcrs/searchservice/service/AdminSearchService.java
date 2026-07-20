package org.ultra.rcrs.searchservice.service;

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;
import org.ultra.rcrs.searchservice.document.AlbumAdminDoc;
import org.ultra.rcrs.searchservice.document.ArtistAdminDoc;
import org.ultra.rcrs.searchservice.document.TrackAdminDoc;
import org.ultra.rcrs.searchservice.dto.*;
import org.ultra.rcrs.searchservice.enums.SearchType;
import org.ultra.rcrs.searchservice.feign.CatalogClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final CatalogClient catalogClient;

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
        List<String> ids = hits.getSearchHits().stream().map(SearchHit::getId).toList();

        try {
            var response = catalogClient.getArtists(ids);
            var list = response.stream().map(ArtistResultWrapper::new).toList();
            return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(e);
        }
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
        List<String> ids = hits.getSearchHits().stream().map(SearchHit::getId).toList();

        try {
            var response = catalogClient.getAlbums(ids);
            var list = response.stream().map(AlbumResultWrapper::new).toList();
            return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(e);
        }
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
        List<String> ids = hits.getSearchHits().stream().map(SearchHit::getId).toList();

        try {
            var response = catalogClient.getTracks(ids);
            var list = response.stream().map(TrackResultWrapper::new).toList();
            return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(e);
        }
    }
}
