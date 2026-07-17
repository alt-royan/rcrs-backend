package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.repository.write.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.write.TrackRepository;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.catalogservice.service.write.TrackWriteService;
import org.ultra.rcrs.enums.EntityStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final TrackWriteService trackWriteService;
    private final AlbumWriteService albumWriteService;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    @Transactional
    public void updateTrackStatus(UUID trackId, EntityStatus status) {
        if (EntityStatus.QUEUED_FOR_TRANSCODING.equals(status) || EntityStatus.TRANSCODING.equals(status)) {
            trackWriteService.updateStatus(trackId, status);
            var track = trackRepository.findById(trackId).orElseThrow();
            albumWriteService.updateStatus(track.getAlbumId(), EntityStatus.TRANSCODING);
        } else if (EntityStatus.FAILED.equals(status)) {
            trackWriteService.updateStatus(trackId, status);
            var track = trackRepository.findById(trackId).orElseThrow();
            albumWriteService.updateStatus(track.getAlbumId(), status);
        } else if (EntityStatus.TRANSCODING_SUCCESS.equals(status)) {
            var track = trackRepository.findById(trackId).orElseThrow();
            var album = albumRepository.findById(track.getAlbumId()).orElseThrow();
            if (isAlbumPublished(album)) {
                trackWriteService.updateStatus(trackId, EntityStatus.READY_FOR_PUBLISHING);
            } else {
                trackWriteService.updateStatus(trackId, status);
                boolean last = trackRepository.countByAlbumIdAndStatus(album.getId(), EntityStatus.TRANSCODING_SUCCESS)
                        == trackRepository.countByAlbumId(album.getId());
                if (last) {
                    albumWriteService.readyForPublishing(album.getId());
                }
            }
        }
    }

    private boolean isAlbumPublished(Album album) {
        return EntityStatus.PUBLISHED.equals(album.getStatus());
    }
}
