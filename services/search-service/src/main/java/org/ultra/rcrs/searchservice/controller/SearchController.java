package org.ultra.rcrs.searchservice.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.searchservice.dto.SearchResponse;
import org.ultra.rcrs.searchservice.enums.SearchType;
import org.ultra.rcrs.searchservice.service.SearchService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text search over the catalog (artists, albums, tracks) backed by Elasticsearch, with a public storefront mode and an unfiltered admin mode.")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/search")
    public ResponseEntity<SearchResponse> search(
            HttpServletRequest request,
            @Parameter(description = "The free-text query to search for; matched against the searchable name/title fields of the requested entity types (e.g. artist name, album title, track title).", required = true)
            @RequestParam("q") String query,
            @Parameter(description = "One or more entity kinds to search: 'artist', 'album' and/or 'track'. Repeat the parameter to search several kinds at once (e.g. type=artist&type=track); each requested kind is searched in parallel and returned in its own section of the response.", required = true)
            @RequestParam(value = "type") SearchType[] types,
            @Parameter(description = "Zero-based page index applied independently to each requested type's result set. Defaults to 0.")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Maximum number of results per page, applied independently to each requested type's result set. Defaults to 20.")
            @RequestParam(value = "size", defaultValue = "20") int size) {
        SearchResponse searchResponse = searchService.searchPublic(types, query, page, size, request);
        return ResponseEntity.ok(searchResponse);
    }

    @GetMapping("/api/admin/search")
    public ResponseEntity<SearchResponse> searchAdmin(
            HttpServletRequest request,
            @Parameter(description = "The free-text query to search for; matched against the searchable name/title fields of the requested entity types (e.g. artist name, album title, track title).", required = true)
            @RequestParam("q") String query,
            @Parameter(description = "One or more entity kinds to search: 'artist', 'album' and/or 'track'. Repeat the parameter to search several kinds at once (e.g. type=artist&type=track); each requested kind is searched in parallel and returned in its own section of the response.", required = true)
            @RequestParam(value = "type") SearchType[] types,
            @Parameter(description = "Zero-based page index applied independently to each requested type's result set. Defaults to 0.")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Maximum number of results per page, applied independently to each requested type's result set. Defaults to 20.")
            @RequestParam(value = "size", defaultValue = "20") int size) {
        SearchResponse searchResponse = searchService.searchAdmin(types, query, page, size, request);
        return ResponseEntity.ok(searchResponse);
    }
}
