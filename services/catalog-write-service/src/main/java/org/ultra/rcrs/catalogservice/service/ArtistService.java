package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.model.*;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistToAlbumRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistToTrackRepository;
import org.ultra.rcrs.catalogservice.repository.OtherArtistRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final OtherArtistRepository otherArtistRepository;
    private final ArtistToTrackRepository artistToTrackRepository;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final CdcService cdcService;
    private final S3Utils s3Utils;

    @Transactional
    public void createArtist(ArtistCreateRequest request) {
        Artist artist = artistRepository.save(Artist.builder()
                .name(request.getName())
                .avatarS3Key(s3Utils.parseKey(request.getAvatarUri()))
                .socialLinks(new SocialLinks(request.getSocialLinks()))
                .tags(request.getTags())
                .availabilityStatus(EntityStatus.ACTIVE)
                .build());
        log.info("Artist {} was created successfully. Artist UUID: {}, public Id: {}",
                request.getName(), artist.getId(), Url62.encode(artist.getId()));
        cdcService.artistCreated(artist.getId());
    }

    @Transactional
    public void deleteArtist(UUID artistId) {
        updateAvailability(EntityStatus.DELETED, artistId);
        cdcService.artistDeleted(artistId);
    }

    @Transactional
    public void hideArtist(UUID artistId) {
        updateAvailability(EntityStatus.HIDDEN, artistId);
        cdcService.artistHidden(artistId);
    }

    public void checkArtistExists(UUID id) {
        if (!artistRepository.existsById(id)) {
            throw new NotFoundException("Artist", id);
        }
    }

    @Transactional
    public void saveArtistsToTrack(List<ArtistIdDto> artists, UUID trackId) {
        artists.forEach(a -> {
            UUID id = Url62.decode(a.getId());
            checkArtistExists(id);
            var artist = artistToTrackRepository.save(ArtistToTrack.builder()
                    .artistId(id)
                    .trackId(trackId)
                    .artistRole(a.getRole())
                    .build());
            log.info("Artist {} with role {} attached to track {}", artist, a.getRole(), trackId);
        });
    }

    @Transactional
    public void saveArtistsToAlbum(List<ArtistIdDto> artists, UUID albumId) {
        artists.forEach(a -> {
            var artist = artistToAlbumRepository.save(ArtistToAlbum.builder()
                    .artistId(Url62.decode(a.getId()))
                    .albumId(albumId)
                    .artistRole(a.getRole())
                    .build());
            log.info("Artist {} with role {} attached to album {}", artist, a.getRole(), albumId);
        });
    }

    @Transactional
    public void saveOthersToTrack(List<OtherArtistDto> others, UUID trackId) {
        others.forEach(o -> {
            var other = otherArtistRepository.save(new OtherArtist(o, trackId));
            log.info("OtherArtist {} saved for track {}", other, trackId);
        });
    }

    private void updateAvailability(EntityStatus status, UUID trackId) {
        artistRepository.updateAvailabilityStatusById(status, trackId);
        log.info("Artist {} availability_status updated to {}", trackId, status);
    }
}
