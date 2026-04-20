package org.ultra.rcrs.searchservice.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;
import org.ultra.rcrs.searchservice.document.AlbumDoc;
import org.ultra.rcrs.searchservice.document.ArtistDoc;
import org.ultra.rcrs.searchservice.document.TrackDoc;
import org.ultra.rcrs.searchservice.dto.*;
import org.ultra.rcrs.searchservice.enums.SearchType;
import org.ultra.rcrs.searchservice.feign.CatalogClient;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final CatalogClient catalogClient;

    public SearchResponse search(SearchType[] types, String query, int page, int size, boolean onlyPublished, HttpServletRequest request) {
        log.info("Searching with types {},  query: {} with offset: {} limit: {}", types, query, page, size);

        SearchResponse response = new SearchResponse();

        var features = Arrays.stream(types).map(t -> switch (t) {
            case artist ->
                    CompletableFuture.supplyAsync(() -> enrichWithHref(this.searchArtists(query, page, size), SearchType.artist, request)).thenAccept(response::setArtists);
            case album ->
                    CompletableFuture.supplyAsync(() -> enrichWithHref(this.searchAlbums(query, page, size, onlyPublished), SearchType.album, request)).thenAccept(response::setAlbums);
            case track ->
                    CompletableFuture.supplyAsync(() -> enrichWithHref(this.searchTracks(query, page, size, onlyPublished), SearchType.track, request)).thenAccept(response::setTracks);
        }).toArray(CompletableFuture<?>[]::new);


        CompletableFuture.allOf(features).join();
        return response;
    }

    //nested queries
    private SearchCollection<ArtistResultWrapper> searchArtists(String query, int page, int size) {
        var query1 = Query.of(q -> q
                .bool(b -> b
                        .must(s -> s.multiMatch(mm -> mm
                                .query(query)
                                .fields("name^2", "albums", "tracks")
                                .type(TextQueryType.BestFields))
                        )
                        .should(s -> s.term(t -> t.field("tags").value(query)))
                )
        );

        var nativeQ = NativeQuery.builder()
                .withQuery(query1)
                .withPageable(PageRequest.of(page, size))
                .build();
        var hits = elasticsearchOperations.search(nativeQ, ArtistDoc.class);
        List<String> ids = hits.get().map(SearchHit::getId).toList();
        try {
            var response = catalogClient.getArtists(ids);
            var list = response.stream().map(ArtistResultWrapper::new).toList();
            return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(e);
        }
    }

    private SearchCollection<AlbumResultWrapper> searchAlbums(String query, int page, int size, boolean onlyPublished) {
        var query1 = Query.of(q -> q
                .bool(b -> {
                            b.must(s -> s.multiMatch(mm -> mm
                                    .query(query)
                                    .fields("title^2", "tracks", "artists")
                                    .type(TextQueryType.BestFields))
                            );
                            if (onlyPublished) {
                                b.must(s -> s.term(t -> t.field("published").value(true)));
                            }
                            return b;
                        }
                )
        );

        var nativeQ = NativeQuery.builder()
                .withQuery(query1)
                .withPageable(PageRequest.of(page, size))
                .build();
        var hits = elasticsearchOperations.search(nativeQ, AlbumDoc.class);
        List<String> ids = hits.get().map(SearchHit::getId).toList();
        try {
            var response = catalogClient.getAlbums(ids);
            var list = response.stream().map(AlbumResultWrapper::new).toList();
            return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(e);
        }
    }

    private SearchCollection<TrackResultWrapper> searchTracks(String query, int page, int size, boolean onlyPublished) {
        var query1 = Query.of(q -> q
                .bool(b -> {
                            b.must(s -> s.multiMatch(mm -> mm
                                    .query(query)
                                    .fields("title^2", "album", "artists")
                                    .type(TextQueryType.BestFields))
                            );
                            if (onlyPublished) {
                                b.must(s -> s.term(t -> t.field("published").value(true)));
                            }
                            return b;
                        }
                )
        );

        var nativeQ = NativeQuery.builder()
                .withQuery(query1)
                .withPageable(PageRequest.of(page, size))
                .build();
        var hits = elasticsearchOperations.search(nativeQ, TrackDoc.class);
        List<String> ids = hits.get().map(SearchHit::getId).toList();
        try {
            var response = catalogClient.getTracks(ids);
            var list = response.stream().map(TrackResultWrapper::new).toList();
            return new SearchCollection<>(query, page, size, hits.getTotalHits(), list);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(e);
        }
    }

    private <T extends ResultWrapper> SearchCollection<T> enrichWithHref(SearchCollection<T> searchCollection, SearchType type, HttpServletRequest request) {
        try {
            if (searchCollection != null && hasNext(searchCollection.getTotal(), searchCollection.getPage(), searchCollection.getSize())) {
                var next = new URIBuilder(request.getRequestURL().toString())
                        .setParameter("q", searchCollection.getQuery())
                        .setParameter("page", String.valueOf(searchCollection.getPage() + 1))
                        .setParameter("size", String.valueOf(searchCollection.getSize()))
                        .setParameter("type", type.name())
                        .setCharset(StandardCharsets.UTF_8)
                        .toString();
                searchCollection.setNext(next);
            }

            return searchCollection;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasNext(long total, int page, int size) {
        return total > (long) (page + 1) * size;
    }
}