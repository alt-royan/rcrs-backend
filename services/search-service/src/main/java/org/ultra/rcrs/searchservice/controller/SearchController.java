package org.ultra.rcrs.searchservice.controller;

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
@RequestMapping
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            HttpServletRequest request,
            @RequestParam(name = "admin", required = false, defaultValue = "false") boolean admin,
            @RequestParam("q") String query,
            @RequestParam(value = "type") SearchType[] types,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        SearchResponse searchResponse;
        if (admin) {
            searchResponse = searchService.searchAdmin(types, query, page, size, request);
        } else {
            searchResponse = searchService.searchPublic(types, query, page, size, request);
        }
        return ResponseEntity.ok(searchResponse);
    }
}
