package org.ultra.rcrs.libraryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.libraryservice.dto.request.SearchHistoryRequest;
import org.ultra.rcrs.libraryservice.dto.response.SearchHistoryEntryDto;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.jpa.SearchHistoryEntry;
import org.ultra.rcrs.libraryservice.model.jpa.SearchHistoryPK;
import org.ultra.rcrs.libraryservice.repository.jpa.SearchHistoryRepository;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    @Value("${library.search-history.max-entries:100}")
    private int maxEntries;

    public List<SearchHistoryEntryDto> list(String userId, int limit) {
        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(entry -> new SearchHistoryEntryDto(
                        entry.getEntityType(), entry.getEntityId(), entry.getQueryRaw(), entry.getSearchedAt()))
                .toList();
    }

    /**
     * Records that the user opened an entity from search results. Re-opening the
     * same entity updates the existing row rather than adding another, so the list
     * stays a set of distinct entities ordered by recency.
     */
    @Transactional
    public void record(String userId, SearchHistoryRequest request) {
        var key = new SearchHistoryPK(userId, request.getEntityType(), request.getEntityId());
        SearchHistoryEntry entry = searchHistoryRepository.findById(key)
                .orElseGet(() -> SearchHistoryEntry.builder()
                        .userId(userId)
                        .entityType(request.getEntityType())
                        .entityId(request.getEntityId())
                        .build());
        entry.setQueryRaw(request.getQuery());
        entry.setSearchedAt(Instant.now());
        searchHistoryRepository.save(entry);

        trim(userId);
    }

    @Transactional
    public void delete(String userId, LibraryEntityType type, String entityId) {
        searchHistoryRepository.deleteById(new SearchHistoryPK(userId, type, entityId));
    }

    @Transactional
    public void clear(String userId) {
        searchHistoryRepository.deleteAllByUserId(userId);
    }

    /**
     * Keeps the table bounded at {@code users x maxEntries}. Reads the timestamp of
     * the oldest entry still inside the cap and drops everything older, which is a
     * single indexed delete rather than a per-row cleanup.
     */
    private void trim(String userId) {
        if (searchHistoryRepository.countByUserId(userId) <= maxEntries) {
            return;
        }
        // Page maxEntries-1 with size 1 is the maxEntries-th newest row: the cut-off.
        List<Instant> boundary = searchHistoryRepository.findSearchedAtDesc(
                userId, PageRequest.of(maxEntries - 1, 1));
        if (boundary.isEmpty()) {
            return;
        }
        int removed = searchHistoryRepository.deleteOlderThan(userId, boundary.get(0));
        log.debug("Trimmed {} search history entries for user {}", removed, userId);
    }
}
