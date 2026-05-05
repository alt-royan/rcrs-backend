package org.ultra.rcrs.catalogservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.repository.write.impl.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.write.impl.TrackRepository;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.catalogservice.service.write.TrackWriteService;
import reactor.core.publisher.Flux;

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
    public Flux<Void> schedulePublish() {
        return albumRepository.findAllReadyForPublishing()
                .map(Album::getId)
                .flatMap(albumWriteService::publishAlbum)
                .thenMany(trackRepository.findAllReadyForPublishing()
                        .map(Track::getId)
                        .flatMap(trackWriteService::publishTrack)
                );
    }
}
