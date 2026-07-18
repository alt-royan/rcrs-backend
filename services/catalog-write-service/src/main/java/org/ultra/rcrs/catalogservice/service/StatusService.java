package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final TrackService trackService;
    private final AlbumService albumService;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    @Transactional
    public void updateTrackStatus(UUID trackId, LifecycleStatus status) {
  /*      if ( LifecycleStatus.TRANSCODING.equals(status)) {
            trackWriteService.updateLifecycleStatus(trackId, status);
            var track = trackRepository.findById(trackId).orElseThrow();
            albumWriteService.updateStatus(track.getAlbumId(), LifecycleStatus.TRANSCODING);
        } else if (LifecycleStatus.FAILED.equals(status)) {
            trackWriteService.updateLifecycleStatus(trackId, status);
            var track = trackRepository.findById(trackId).orElseThrow();
            albumWriteService.updateStatus(track.getAlbumId(), status);
        } else if (LifecycleStatus.READY.equals(status)) {
            var track = trackRepository.findById(trackId).orElseThrow();
            var album = albumRepository.findById(track.getAlbumId()).orElseThrow();
            if (isAlbumPublished(album)) {
                trackWriteService.updateLifecycleStatus(trackId, LifecycleStatus.READY);
            } else {
                trackWriteService.updateLifecycleStatus(trackId, status);
                boolean last = trackRepository.countByAlbumIdAndStatus(album.getId(), LifecycleStatus.READY)
                        == trackRepository.countByAlbumId(album.getId());
                if (last) {
                    albumWriteService.readyForPublishing(album.getId());
                }
            }
        }*/
    }
}
