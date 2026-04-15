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
import org.ultra.rcrs.searchservice.document.ArtistDoc;
import org.ultra.rcrs.searchservice.dto.*;
import org.ultra.rcrs.searchservice.enums.SearchType;
import org.ultra.rcrs.searchservice.feign.CatalogClient;

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
    private final HttpServletRequest request;


    public SearchResponse search(SearchType[] types, String query, int page, int size) {
        log.info("Searching with types {},  query: {} with offset: {} limit: {}", types, query, page, size);

        SearchResponse response = new SearchResponse();

        var features = Arrays.stream(types).map(t -> switch (t) {
            case ARTIST -> CompletableFuture.supplyAsync(() -> this.searchArtists(query, page, size))
                    .thenAccept(r ->
                            response.setArtists(enrichWithHref(r, SearchType.ARTIST, query)));
            case ALBUM -> CompletableFuture.supplyAsync(() -> this.searchAlbums(query, page, size))
                    .thenAccept(r ->
                            response.setAlbums(enrichWithHref(r, SearchType.ALBUM, query)));
            case TRACK -> CompletableFuture.supplyAsync(() -> this.searchTracks(query, page, size))
                    .thenAccept(r ->
                            response.setTracks(enrichWithHref(r, SearchType.TRACK, query)));
        }).toArray(CompletableFuture<?>[]::new);


        CompletableFuture.allOf(features);
        return response;
    }

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
            return new SearchCollection<>(page, size, hits.getTotalHits(), list);
        } catch (FeignException e) {
            throw new ServiceUnavailableException(e);
        }
    }

    private SearchCollection<AlbumResultWrapper> searchAlbums(String query, int page, int size) {
        return null;
    }

    private SearchCollection<TrackResultWrapper> searchTracks(String query, int page, int size) {
        return null;
    }

    private <T extends ResultWrapper> SearchCollection<T> enrichWithHref(SearchCollection<T> searchCollection, SearchType type, String q) {
        if (searchCollection != null && hasNext(searchCollection.getTotal(), searchCollection.getPage(), searchCollection.getSize())) {
            var next = new URIBuilder().setHost(request.getRemoteHost())
                    .setPath(request.getServletPath())
                    .setParameter("q", q)
                    .setParameter("page", String.valueOf(searchCollection.getPage() + 1))
                    .setParameter("size", String.valueOf(searchCollection.getSize()))
                    .setParameter("type", type.getValue())
                    .setCharset(StandardCharsets.UTF_8)
                    .toString();
            searchCollection.setNext(next);
        }
        return searchCollection;
    }

    private boolean hasNext(long total, int page, int size) {
        return total > (long) (page + 1) * size;
    }
}