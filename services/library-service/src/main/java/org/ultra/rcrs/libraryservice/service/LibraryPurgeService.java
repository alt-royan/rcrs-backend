package org.ultra.rcrs.libraryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.libraryservice.repository.jpa.*;

/**
 * Erases everything this service holds about one user, across both stores.
 * <p>
 * The relational deletes share one transaction; the Mongo removes cannot join it,
 * but they run inside its boundary, so a Mongo failure rolls the relational half
 * back and the retry starts from a consistent state. Every step is idempotent, so
 * a redelivered identity event is harmless.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryPurgeService {

    private final LikeRepository likeRepository;
    private final DownloadedTrackRepository downloadedTrackRepository;
    private final DownloadedCollectionRepository downloadedCollectionRepository;
    private final DownloadedCollectionTrackRepository downloadedCollectionTrackRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final ListenService listenService;

    @Transactional
    public void purge(String userId) {
        likeRepository.deleteAllByUserId(userId);
        searchHistoryRepository.deleteAllByUserId(userId);
        // Membership first: it is the thing that keeps downloaded tracks referenced.
        downloadedCollectionTrackRepository.deleteAllByUserId(userId);
        downloadedCollectionRepository.deleteAllByUserId(userId);
        downloadedTrackRepository.deleteAllByUserId(userId);

        listenService.purge(userId);
        log.info("Purged library data for user {}", userId);
    }
}
