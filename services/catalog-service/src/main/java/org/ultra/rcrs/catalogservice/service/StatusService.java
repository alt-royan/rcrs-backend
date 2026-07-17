package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.repository.write.impl.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.write.impl.TrackRepository;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.catalogservice.service.write.TrackWriteService;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final TrackWriteService trackWriteService;
    private final AlbumWriteService albumWriteService;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    public Mono<Void> updateTrackStatus(UUID trackId, EntityStatus status) {
        Mono<Void> mono = Mono.empty();
        if ( EntityStatus.TRANSCODING.equals(status)) {
            mono = trackWriteService.updateStatus(trackId, status)
                    .flatMap(v -> trackRepository.findById(trackId)
                            .flatMap(t -> albumWriteService.updateStatus(t.getAlbumId(), EntityStatus.TRANSCODING)));
        } else if (EntityStatus.FAILED.equals(status)) {
            mono = trackWriteService.updateStatus(trackId, status)
                    .flatMap(v -> trackRepository.findById(trackId)
                            .flatMap(t -> albumWriteService.updateStatus(t.getAlbumId(), status)));
        } else if (EntityStatus.READY.equals(status)) {
            mono = getAlbumForTrack(trackId).flatMap(album -> {
                if (isAlbumPublished(album)) {
                    return trackWriteService.updateStatus(trackId, EntityStatus.READY);
                } else {
                    return trackWriteService.updateStatus(trackId, status)
                            .flatMap(v -> isThisLastTranscodeTrack(album.getId())
                                    .flatMap(last -> {
                                        if (last) {
                                            return albumWriteService.readyForPublishing(album.getId());
                                        }
                                        return Mono.empty();
                                    })
                            );
                }
            });
        }

        return mono;
    }

    private Mono<Album> getAlbumForTrack(UUID trackId) {
        return trackRepository.findById(trackId)
                .flatMap(t -> albumRepository.findById(t.getAlbumId()));
    }

    private boolean isAlbumPublished(Album album) {
        return EntityStatus.PUBLISHED.equals(album.getStatus());
    }

    private Mono<Boolean> isThisLastTranscodeTrack(UUID albumId) {
        return trackRepository.allTracksInAlbumHaveStatus(albumId, EntityStatus.READY);
    }

}
