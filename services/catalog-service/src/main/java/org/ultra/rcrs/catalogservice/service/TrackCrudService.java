package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.TrackMetadataAbstract;
import org.ultra.rcrs.catalogservice.dto.full.FullTrackMetadata;
import org.ultra.rcrs.catalogservice.dto.request.TrackCreateRequest;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.catalogservice.service.operations.AlbumOperationService;
import org.ultra.rcrs.catalogservice.service.operations.ArtistOperationService;
import org.ultra.rcrs.catalogservice.service.operations.TrackOperationService;
import org.ultra.rcrs.enums.TrackStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackCrudService {

    private final TrackRepository trackRepository;
    private final AlbumOperationService albumService;
    public final ArtistOperationService artistService;
    public final TrackOperationService trackService;

    public Mono<FullTrackMetadata> getTrack(UUID trackId, boolean published) {
        Mono<Track> track;

        if (published) {
            track = trackRepository.findById(trackId)
                    .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")))
                    .map(t -> {
                        t.getKey().setStatus(null);
                        return t;
                    });
        } else {
            track = trackRepository.findByKeyIdAndKeyStatus(trackId, TrackStatus.PUBLISHED)
                    .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")));
        }

        return track.zipWith(artistService.collectArtistsWithRoleForTrack(trackId))
                .flatMap(tuple -> albumService.collectAlbumForTrack(tuple.getT1().getAlbumId())
                        .map(album -> new FullTrackMetadata(tuple.getT1(), album, tuple.getT2())));
    }

    public Mono<TrackMetadataAbstract> createTrack(TrackCreateRequest dto) {
        return trackRepository.save(new Track(dto)).flatMap(this::trackToDto);
    }

    public Mono<Void> deleteTrackCascadeById(UUID trackId) {
        //Добавить декримент альбома
        return trackService.deleteTrackById(trackId);
    }
}
