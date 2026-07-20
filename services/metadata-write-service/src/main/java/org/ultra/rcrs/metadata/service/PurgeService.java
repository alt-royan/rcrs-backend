package org.ultra.rcrs.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.repository.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurgeService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final OtherArtistRepository otherArtistRepository;
    private final ArtistToTrackRepository artistToTrackRepository;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final CatalogEventProducer catalogEventProducer;

    @Transactional
    public void purge() {
        purgeTracks();
        purgeAlbums();
        purgeArtists();
    }

    private void purgeTracks() {
        List<UUID> deletedTrackIds = trackRepository.findAllByAvailabilityStatus(EntityStatus.DELETED);
        if (deletedTrackIds.isEmpty()) return;

        otherArtistRepository.deleteByTrackIdsIn(deletedTrackIds);
        artistToTrackRepository.deleteByTrackIdsIn(deletedTrackIds);
        trackRepository.deleteAllByIdInBatch(deletedTrackIds);

        for (UUID trackId : deletedTrackIds) {
            catalogEventProducer.trackTrueDeleted(trackId);
        }
        log.info("Purged {} tracks", deletedTrackIds.size());
    }

    private void purgeAlbums() {
        List<UUID> deletedAlbumIds = albumRepository.findAllByAvailabilityStatus(EntityStatus.DELETED);
        if (deletedAlbumIds.isEmpty()) return;

        artistToAlbumRepository.deleteByAlbumIdsIn(deletedAlbumIds);
        albumRepository.deleteAllByIdInBatch(deletedAlbumIds);

        for (UUID albumId : deletedAlbumIds) {
            catalogEventProducer.albumTrueDeleted(albumId);
        }
        log.info("Purged {} albums", deletedAlbumIds.size());
    }

    private void purgeArtists() {
        List<UUID> deletedArtistIds = artistRepository.findAllByAvailabilityStatus(EntityStatus.DELETED);
        if (deletedArtistIds.isEmpty()) return;

        artistRepository.deleteAllByIdInBatch(deletedArtistIds);

        for (UUID artistId : deletedArtistIds) {
            catalogEventProducer.artistTrueDeleted(artistId);
        }
        log.info("Purged {} artists", deletedArtistIds.size());
    }
}
