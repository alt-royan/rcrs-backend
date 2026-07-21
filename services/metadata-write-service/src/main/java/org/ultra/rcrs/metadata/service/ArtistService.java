package org.ultra.rcrs.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.metadata.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.Artist;
import org.ultra.rcrs.metadata.model.SocialLinks;
import org.ultra.rcrs.metadata.repository.ArtistRepository;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final AlbumService albumService;
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
        var albums = albumService.findAllIdsByArtist(artistId);
        albums.forEach(albumService::markAlbumDelete);

        updateAvailability(EntityStatus.DELETED, artistId);
    }

    @Transactional
    public void hideArtist(UUID artistId) {
        var albums = albumService.findAllIdsByArtist(artistId);
        albums.forEach(albumService::hideAlbum);

        updateAvailability(EntityStatus.HIDDEN, artistId);
    }

    @Transactional
    public void activeArtist(UUID artistId) {
        var albums = albumService.findAllIdsByArtist(artistId);
        albums.forEach(albumService::activeAlbum);

        updateAvailability(EntityStatus.ACTIVE, artistId);
    }

    public boolean artistExists(UUID id) {
        return artistRepository.existsById(id);
    }

    private void updateAvailability(EntityStatus status, UUID artistId) {
        artistRepository.updateAvailabilityStatusById(status, artistId);
        switch (status) {
            case ACTIVE -> catalogEventProducer.artistActivated(artistId);
            case HIDDEN -> catalogEventProducer.artistHidden(artistId);
            case DELETED -> catalogEventProducer.artistDeleted(artistId);
        }
        log.info("Artist {} availability_status updated to {}", artistId, status);
    }
}
