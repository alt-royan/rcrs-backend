package org.ultra.rcrs.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.metadata.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.*;
import org.ultra.rcrs.metadata.repository.AlbumRepository;
import org.ultra.rcrs.metadata.repository.ArtistRepository;
import org.ultra.rcrs.metadata.repository.TrackRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final CatalogEventProducer catalogEventProducer;
    private final S3Utils s3Utils;

    @Transactional
    public UUID createArtist(ArtistCreateRequest request) {
        Artist artist = artistRepository.save(Artist.builder()
                .name(request.getName())
                .avatarS3Key(s3Utils.parseKey(request.getAvatarUri()))
                .socialLinks(new SocialLinks(request.getSocialLinks()))
                .tags(request.getTags())
                .availabilityStatus(EntityStatus.ACTIVE)
                .build());
        log.info("Artist {} was created successfully. Artist UUID: {}, public Id: {}",
                request.getName(), artist.getId(), Url62.encode(artist.getId()));
        catalogEventProducer.artistCreated(artist);
        return artist.getId();
    }

    @Transactional
    public void markArtistDelete(UUID artistId) {
        updateAvailability(EntityStatus.DELETED, artistId);
        albumRepository.updateAvailabilityStatusByArtistId(EntityStatus.DELETED, artistId);
        trackRepository.updateAvailabilityStatusByArtistId(EntityStatus.DELETED, artistId);
        catalogEventProducer.artistDeleted(artistId);
        log.info("Artist {} and all related albums/tracks marked as DELETED", artistId);
    }

    @Transactional
    public void hideArtist(UUID artistId) {
        updateAvailability(EntityStatus.HIDDEN, artistId);
        albumRepository.updateAvailabilityStatusByArtistId(EntityStatus.HIDDEN, artistId);
        trackRepository.updateAvailabilityStatusByArtistId(EntityStatus.HIDDEN, artistId);
        catalogEventProducer.artistHidden(artistId);
        log.info("Artist {} and all related albums/tracks marked as HIDDEN", artistId);
    }

    @Transactional
    public void activeArtist(UUID artistId) {
        updateAvailability(EntityStatus.ACTIVE, artistId);
        albumRepository.updateAvailabilityStatusByArtistId(EntityStatus.ACTIVE, artistId);
        trackRepository.updateAvailabilityStatusByArtistId(EntityStatus.ACTIVE, artistId);
        catalogEventProducer.artistActivated(artistId);
        log.info("Artist {} and all related albums/tracks marked as ACTIVE", artistId);
    }

    public boolean artistExists(UUID id) {
        return artistRepository.existsById(id);
    }

    private void updateAvailability(EntityStatus status, UUID trackId) {
        artistRepository.updateAvailabilityStatusById(status, trackId);
        log.info("Artist {} availability_status updated to {}", trackId, status);
    }
}
