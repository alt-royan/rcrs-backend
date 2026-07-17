package org.ultra.rcrs.catalogservice.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.kafka.producer.EventProducer;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrack;
import org.ultra.rcrs.catalogservice.model.write.OtherArtist;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.repository.AfterCommit;
import org.ultra.rcrs.catalogservice.repository.write.impl.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.write.impl.ArtistToTrackRepository;
import org.ultra.rcrs.catalogservice.repository.write.impl.OtherArtistRepository;
import org.ultra.rcrs.catalogservice.repository.write.impl.TrackRepository;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackWriteService {

    private final TrackRepository trackRepository;
    private final OtherArtistRepository otherArtistRepository;
    private final ArtistToTrackRepository artistToTrackRepository;
    private final ArtistRepository artistRepository;
    private final EventProducer eventProducer;

    @Transactional
    public Flux<Void> createTracks(List<TrackUploadRequest> requests, UUID albumId) {
        return Flux.fromIterable(requests).flatMap(r -> this.createTrack(r, albumId));
    }

    @Transactional
    public Mono<Void> createTrack(TrackUploadRequest uploadRequest, UUID albumId) {
        UUID trackId = UUID.randomUUID();
        return checkArtists(uploadRequest.getArtists())
                .then(trackRepository.insert(Track.builder()
                                .id(trackId)
                                .status(LifecycleStatus.CREATED)
                                .title(uploadRequest.getTitle())
                                .releaseDate(uploadRequest.getReleaseDate())
                                .durationMs(0)
                                .trackNumber(uploadRequest.getTrackNumber())
                                .explicit(uploadRequest.getExplicit())
                                .available(true)
                                .albumId(albumId)
                                .build())
                        .flatMap(t -> AfterCommit.log("Track {} saved with id {}", t.getTitle(), t.getId())
                                .thenMany(saveArtistsToTrack(uploadRequest.getArtists(), t.getId()))
                                .thenMany(saveOthersToTrack(uploadRequest.getOthers(), t.getId()))
                                .then(AfterCommit.execute(eventProducer.trackCreated(uploadRequest.getUid(), t.getId())))));
    }

    @Transactional
    public Mono<Void> deleteTrack(UUID trackId) {
        return otherArtistRepository.deleteByTrackId(trackId)
                .flatMap(l -> artistToTrackRepository.deleteByTrackId(trackId))
                .flatMap(l -> trackRepository.delete(trackId))
                .flatMap(l -> AfterCommit.log("Track {} deleted", trackId)
                        .then(AfterCommit.execute(eventProducer.trackDeleted(trackId)))
                );
    }

    private Mono<Void> checkArtists(List<ArtistIdDto> artists) {
        return Flux.fromIterable(artists)
                .flatMap(a -> {
                    var id = Url62.decode(a.getId());
                    return artistRepository.existsById(id)
                            .flatMap(exists -> {
                                if (!exists)
                                    return Mono.error(new NotFoundException("Artist", id));
                                return Mono.empty();
                            });
                }).then();
    }

    private Flux<Void> saveArtistsToTrack(List<ArtistIdDto> artists, UUID trackId) {
        return Flux.fromIterable(artists)
                .map(a -> ArtistToTrack.builder()
                        .artistId(Url62.decode(a.getId()))
                        .trackId(trackId)
                        .artistRole(a.getRole())
                        .build())
                .flatMap(artistToTrackRepository::insert)
                .flatMap(a -> AfterCommit.log("Artist {} with role {} attached to track {}",
                        a.getArtistId(), a.getArtistRole(), a.getTrackId()));
    }

    private Flux<Void> saveOthersToTrack(List<OtherArtistDto> others, UUID trackId) {
        return Flux.fromIterable(others)
                .map(o -> new OtherArtist(o, trackId))
                .flatMap(otherArtistRepository::insert)
                .flatMap(o -> AfterCommit.log("OtherArtist {} saved with for track {}",
                        o.getName(), o.getTrackId()));
    }

    @Transactional
    public Mono<Void> updateStatus(UUID trackId, LifecycleStatus status) {
        return trackRepository.updateStatus(trackId, status)
                .flatMap(count ->
                        AfterCommit.log("Update track {} status to {}: {} rows updated", trackId, status, count));
    }

    @Transactional
    public Mono<Void> updateStatusForAllInAlbum(UUID albumId, LifecycleStatus status) {
        return trackRepository.updateStatusForAllInAlbum(albumId, status)
                .flatMap(count ->
                        AfterCommit.log("Update status to {} for all tracks in album {}: {} rows updated", status, albumId, count));
    }

    @Transactional
    public Mono<Void> publishTrack(UUID id) {
        return trackRepository.updateStatusAndReleaseDate(id, LifecycleStatus.PUBLISHED, Instant.now())
                .flatMap(c -> AfterCommit.log("Track {} published", id));
    }

    @Transactional
    public Mono<Void> publishTracks(List<UUID> ids) {
        return Flux.fromIterable(ids).flatMap(this::publishTrack).then();
    }

}
