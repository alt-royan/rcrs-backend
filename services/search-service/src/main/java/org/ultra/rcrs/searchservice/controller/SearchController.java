package org.ultra.rcrs.searchservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.searchservice.dto.SearchResponse;
import org.ultra.rcrs.searchservice.enums.SearchType;
import org.ultra.rcrs.searchservice.producer.EventProducer;
import org.ultra.rcrs.searchservice.service.SearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;
    private final EventProducer eventProducer;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam("q") String query,
            @RequestParam(value = "type") SearchType[] types,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        var result = searchService.search(types, query, size, page, true);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reindex/artists")
    public ResponseEntity<Void> reindexArtists() {
        eventProducer.reindexArtists();
        return ResponseEntity.ok(null);
    }

    @PostMapping("/reindex/albums")
    public ResponseEntity<Void> reindexAlbums() {
        eventProducer.reindexAlbums();
        return ResponseEntity.ok(null);
    }

    @PostMapping("/reindex/tracks")
    public ResponseEntity<Void> reindexTracks() {
        eventProducer.reindexTracks();
        return ResponseEntity.ok(null);
    }

    @PostMapping("/reindex/full")
    public ResponseEntity<Void> reindexFull() {
        eventProducer.reindexArtists();
        eventProducer.reindexAlbums();
        eventProducer.reindexTracks();
        return ResponseEntity.ok(null);
    }

}