package org.ultra.rcrs.catalogservice.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.dto.response.IdResponse;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.model.write.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.repository.write.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.write.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.write.ArtistToAlbumRepository;
import org.ultra.rcrs.catalogservice.repository.write.TrackRepository;
import org.ultra.rcrs.catalogservice.service.CdcService;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumWriteService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final TrackWriteService trackWriteService;
    private final TrackRepository trackRepository;
    private final S3Utils s3Utils;
    private final CdcService cdcService;

    @Transactional
    public IdResponse createAlbum(AlbumUploadRequest request) {
        if (request.getTracks() != null && !request.getTracks().isEmpty()) {
            Set<Integer> numbers = request.getTracks().stream()
                    .map(TrackUploadRequest::getTrackNumber)
                    .collect(Collectors.toSet());

            int size = request.getTracks().size();

            if (numbers.size() != size || !IntStream.rangeClosed(1, size).allMatch(numbers::contains)) {
                throw new BadRequestException("trackNumbers are wrong");
            }

            request.getTracks().forEach(t -> t.setReleaseDate(request.getReleaseDate()));
        }

        UUID albumId = UUID.randomUUID();
        checkArtists(request.getArtists());
        albumRepository.save(Album.builder()
                .id(albumId)
                .status(EntityStatus.CREATED)
                .title(request.getTitle())
                .type(request.getType())
                .releaseDate(request.getReleaseDate())
                .coverS3Key(s3Utils.parseKey(request.getCoverUri()))
                .available(true)
                .build());
        log.info("Album {} saved with id {}", request.getTitle(), albumId);
        saveArtistsToAlbum(request.getArtists(), albumId);
        trackWriteService.createTracks(request.getTracks(), albumId);
        cdcService.albumCreated(albumId);
        return new IdResponse(Url62.encode(albumId));
    }

    @Transactional
    public void deleteAlbum(UUID albumId) {
        List<Track> tracks = trackRepository.findAllByAlbumId(albumId);
        tracks.forEach(track -> trackWriteService.deleteTrack(track.getId()));
        artistToAlbumRepository.deleteByAlbumId(albumId);
        albumRepository.deleteById(albumId);
        log.info("Album {} deleted", albumId);
        cdcService.albumDeleted(albumId);
    }

    private void checkArtists(List<ArtistIdDto> artists) {
        artists.forEach(a -> {
            var id = Url62.decode(a.getId());
            if (!artistRepository.existsById(id)) {
                throw new NotFoundException("Artist", id);
            }
        });
    }

    private void saveArtistsToAlbum(List<ArtistIdDto> artists, UUID albumId) {
        artists.forEach(a -> {
            artistToAlbumRepository.save(ArtistToAlbum.builder()
                    .artistId(Url62.decode(a.getId()))
                    .albumId(albumId)
                    .artistRole(a.getRole())
                    .build());
            log.info("Artist {} with role {} attached to album {}", a.getArtistId(), a.getRole(), albumId);
        });
    }

    @Transactional
    public void updateStatus(UUID albumId, EntityStatus status) {
        int count = albumRepository.updateStatus(albumId, status);
        log.info("Update album {} status to {}: {} rows updated", albumId, status, count);
    }

    @Transactional
    public void readyForPublishing(UUID albumId) {
        int count = albumRepository.updateStatus(albumId, EntityStatus.READY_FOR_PUBLISHING);
        log.info("Update album {} status to {}: {} rows updated", albumId, EntityStatus.READY_FOR_PUBLISHING, count);
        trackWriteService.updateStatusForAllInAlbum(albumId, EntityStatus.READY_FOR_PUBLISHING);
    }

    @Transactional
    public void publishAlbum(UUID id) {
        int c = albumRepository.updateStatusAndReleaseDate(id, EntityStatus.PUBLISHED, Instant.now());
        log.info("Album {} published", id);
        List<Track> tracks = trackRepository.findAllByAlbumId(id);
        trackWriteService.publishTracks(tracks.stream().map(Track::getId).toList());
        cdcService.albumUpserted(id);
    }
}
