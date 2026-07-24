package org.ultra.rcrs.searchservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.searchservice.dto.SearchResponse;
import org.ultra.rcrs.searchservice.enums.SearchType;
import org.ultra.rcrs.searchservice.service.SearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            HttpServletRequest request,
            @RequestParam("q") String query,
            @RequestParam(value = "type") SearchType[] types,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        var result = searchService.searchPublic(types, query, page, size, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/search")
    public ResponseEntity<SearchResponse> searchAdmin(
            HttpServletRequest request,
            @RequestParam("q") String query,
            @RequestParam(value = "type") SearchType[] types,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        var result = searchService.searchAdmin(types, query, page, size, request);
        return ResponseEntity.ok(result);
    }
}
