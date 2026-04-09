package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.ArtistOtherDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumSimpleDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.kafka.producer.EventProducer;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrack;
import org.ultra.rcrs.catalogservice.model.write.OthersOnTrack;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.repository.*;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackCrudService {

    private final TrackViewRepository trackViewRepository;
    private final TrackRepository trackRepository;
    private final ArtistConverter artistConverter;
    private final OthersOnTrackRepository othersOnTrackRepository;
    private final ArtistToTrackRepository artistToTrackRepository;
    private final ArtistRepository artistRepository;
    private final S3Utils s3Utils;
    private final EventProducer eventProducer;

    public Mono<TrackFullDto> getTrack(UUID trackId, List<EntityStatus> statuses) {
        return trackViewRepository.findByIdAndStatusIn(trackId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")))
                .zipWith(othersOnTrackRepository.findByTrackId(trackId))
                .map(tuple -> {
                    var track = tuple.getT1();
                    var others = tuple.getT2();
                    return TrackFullDto.builder()
                            .id(Url62.encode(track.getId()))
                            .status(track.getStatus())
                            .title(track.getTitle())
                            .releaseDate(track.getReleaseDate())
                            .durationMs(track.getDurationMs())
                            .trackNumber(track.getTrackNumber())
                            .explicit(track.getExplicit())
                            .available(track.getAvailable())
                            .album(AlbumSimpleDto.builder()
                                    .id(Url62.encode(track.getAlbum().getId()))
                                    .title(track.getAlbum().getTitle())
                                    .coverUrl(s3Utils.parseUrl(track.getAlbum().getCoverS3Key())).build())
                            .artists(artistConverter.onTackToDto(track.getArtists()))
                            .others(others.getOthers().stream().map(ArtistOtherDto::new).toList())
                            .build();
                });
    }

/*    public Mono<List<TrackInAlbumDto>> getTracksForAlbum(UUID albumId, List<EntityStatus> statuses) {
        return trackConverter.collectTracksForAlbum(albumId, statuses);
    }*/

    @Transactional
    public Flux<Void> saveTracks(List<TrackUploadRequest> requests, UUID albumId) {
        return Flux.fromIterable(requests).flatMap(r -> this.saveTrack(r, albumId));
    }

    @Transactional
    public Mono<Void> saveTrack(TrackUploadRequest uploadRequest, UUID albumId) {
        UUID trackId = UUID.randomUUID();
        return checkArtists(uploadRequest.getArtists())
                .then(trackRepository.save(Track.builder()
                        .id(trackId)
                        .status(EntityStatus.CREATED)
                        .title(uploadRequest.getTitle())
                        .releaseDate(uploadRequest.getReleaseDate())
                        .durationMs(0)
                        .trackNumber(uploadRequest.getTrackNumber())
                        .explicit(uploadRequest.getExplicit())
                        .available(true)
                        .albumId(albumId)
                        .build()))
                .thenMany(saveArtistsToTrack(uploadRequest.getArtists(), trackId))
                .then(saveOthersToTrack(uploadRequest.getOthers(), trackId))
                .doOnSuccess(v -> log.info("Track {} saved with id {}", uploadRequest.getTitle(), trackId))
                .then(Mono.fromRunnable(() -> eventProducer.trackCreated(uploadRequest.getUid(), trackId)))
                .then();
    }

    private Flux<Void> checkArtists(List<ArtistIdDto> artists) {
        return Flux.fromIterable(artists)
                .flatMap(a -> {
                    var id = Url62.decode(a.getId());
                    return artistRepository.existsById(id)
                            .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + id + " was not found")))
                            .then();
                });
    }

    private Flux<ArtistToTrack> saveArtistsToTrack(List<ArtistIdDto> artists, UUID trackId) {
        return Flux.fromIterable(artists)
                .map(a -> ArtistToTrack.builder()
                        .id(new ArtistToTrack.ArtistToTrackId(Url62.decode(a.getId()), trackId))
                        .artistRole(a.getRole())
                        .build())
                .flatMap(a -> artistToTrackRepository.save(a)
                        .doOnSuccess(v ->
                                log.info("Artist {} with role {} attached to track {}", a.getId(), a.getArtistRole(), trackId)));
    }

    private Mono<OthersOnTrack> saveOthersToTrack(List<ArtistOtherDto> others, UUID trackId) {
        return Flux.fromIterable(others)
                .map(OthersOnTrack.ArtistOther::new)
                .collectList()
                .map(o -> OthersOnTrack.builder().trackId(trackId).others(o).build())
                .flatMap(othersOnTrackRepository::save)
                .doOnSuccess(v -> log.info("Others saved with for track {}", trackId));
    }


}
