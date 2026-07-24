package org.ultra.rcrs.searchservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.dto.ResultWrapper;
import org.ultra.rcrs.searchservice.dto.SearchCollection;
import org.ultra.rcrs.searchservice.dto.SearchResponse;
import org.ultra.rcrs.searchservice.enums.SearchType;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final AdminSearchService adminSearchService;
    private final PublicSearchService publicSearchService;

    public SearchResponse searchAdmin(SearchType[] types, String query, int page, int size, HttpServletRequest request) {
        return executeSearch(types, query, page, size, request, true);
    }

    public SearchResponse searchPublic(SearchType[] types, String query, int page, int size, HttpServletRequest request) {
        return executeSearch(types, query, page, size, request, false);
    }

    private SearchResponse executeSearch(SearchType[] types, String query, int page, int size, HttpServletRequest request, boolean admin) {
        log.debug("Searching with types {}, query: {}, page: {}, size: {}, admin: {}", types, query, page, size, admin);

        SearchResponse response = new SearchResponse();

        var features = Arrays.stream(types).map(t -> switch (t) {
            case artist -> CompletableFuture.supplyAsync(() -> enrichWithHref(
                    admin ? adminSearchService.searchArtists(query, page, size)
                            : publicSearchService.searchArtists(query, page, size),
                    SearchType.artist, request)).thenAccept(response::setArtists);
            case album -> CompletableFuture.supplyAsync(() -> enrichWithHref(
                    admin ? adminSearchService.searchAlbums(query, page, size)
                            : publicSearchService.searchAlbums(query, page, size),
                    SearchType.album, request)).thenAccept(response::setAlbums);
            case track -> CompletableFuture.supplyAsync(() -> enrichWithHref(
                    admin ? adminSearchService.searchTracks(query, page, size)
                            : publicSearchService.searchTracks(query, page, size),
                    SearchType.track, request)).thenAccept(response::setTracks);
        }).toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(features).join();
        return response;
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
