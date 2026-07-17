package org.ultra.rcrs.catalogservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.repository.write.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.write.TrackRepository;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.catalogservice.service.write.TrackWriteService;
import org.ultra.rcrs.enums.EntityStatus;

@Component
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class PublishScheduler {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final AlbumWriteService albumWriteService;
    private final TrackWriteService trackWriteService;

    @Scheduled(fixedRate = 60000)
    public void schedulePublish() {
        albumRepository.findAllReadyForPublishing(EntityStatus.READY_FOR_PUBLISHING, java.time.Instant.now())
                .forEach(album -> albumWriteService.publishAlbum(album.getId()));
        trackRepository.findAllReadyForPublishing(EntityStatus.READY_FOR_PUBLISHING, java.time.Instant.now())
                .forEach(track -> trackWriteService.publishTrack(track.getId()));
    }
}
