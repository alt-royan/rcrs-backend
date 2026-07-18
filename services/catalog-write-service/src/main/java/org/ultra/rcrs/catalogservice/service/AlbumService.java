package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.request.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.kafka.CatalogEventProducer;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.model.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.model.ArtistToAlbumPK;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistToAlbumRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final ArtistService artistService;
    private final S3Utils s3Utils;
    private final CatalogEventProducer catalogEventProducer;

    @Transactional
    public void createAlbum(AlbumUploadRequest request) {
        Album album = albumRepository.save(Album.builder()
                .lifecycleStatus(LifecycleStatus.CREATED)
                .title(request.getTitle())
                .type(request.getType())
                .releaseDate(request.getReleaseDate())
                .coverS3Key(s3Utils.parseKey(request.getCoverUri()))
                .availabilityStatus(EntityStatus.ACTIVE)
                .build());

        log.info("Album {} saved with id {}", request.getTitle(), album.getId());
        catalogEventProducer.albumCreated(album);

        addAllArtistToAlbum(request.getArtists(), album.getId());
    }

    @Transactional
    public void deleteAlbum(UUID albumId) {
        updateAvailability(EntityStatus.DELETED, albumId);
        catalogEventProducer.albumDeleted(albumId);
    }

    @Transactional
    public void hideAlbum(UUID albumId) {
        updateAvailability(EntityStatus.HIDDEN, albumId);
        catalogEventProducer.albumHidden(albumId);
    }

    @Transactional
    public void updateLifecycleStatus(LifecycleStatus status, UUID albumId) {
        albumRepository.updateLifecycleStatusById(status, albumId);
        log.info("Album {} lifecycle_status updated to {}", albumId, status);
    }

    @Transactional
    public void addAllArtistToAlbum(List<ArtistDto> artists, UUID albumId) {
        artists.forEach(a -> addArtistToAlbum(a, albumId));
    }

    private void addArtistToAlbum(ArtistDto artistDto, UUID albumUuid) {
        if (!albumExists(albumUuid)) {
            throw new NotFoundException("Album", albumUuid);
        }

        UUID artistUuid = Url62.decode(artistDto.getId());
        if (!artistService.artistExists(artistUuid)) {
            throw new NotFoundException("Artist", artistUuid);
        }

        var artist = artistToAlbumRepository.save(ArtistToAlbum.builder()
                .artistId(artistUuid)
                .albumId(albumUuid)
                .artistRole(artistDto.getRole())
                .build());

        log.info("Artist {} with role {} attached to album {}", artist, artistDto.getRole(), albumUuid);
        catalogEventProducer.artistAddedToAlbum(artistUuid, albumUuid, artistDto.getRole());
    }

    @Transactional
    public void deleteAllArtistFromAlbum(List<ArtistDto> artists, UUID albumId) {
        artists.forEach(a -> deleteArtistFromAlbum(a, albumId));
    }

    private void deleteArtistFromAlbum(ArtistDto artistDto, UUID albumUuid) {
        UUID artistUuid = Url62.decode(artistDto.getId());

        artistToAlbumRepository.deleteById(new ArtistToAlbumPK(artistUuid, albumUuid));

        log.info("Artist {} deleted from album {}", artistUuid, albumUuid);
        catalogEventProducer.artistDeletedFromAlbum(artistUuid, albumUuid);
    }

    private void updateAvailability(EntityStatus status, UUID albumId) {
        albumRepository.updateAvailabilityStatusById(status, albumId);
        log.info("Album {} availability_status updated to {}", albumId, status);
    }

    private boolean albumExists(UUID albumId) {
        return albumRepository.existsById(albumId);
    }
}
