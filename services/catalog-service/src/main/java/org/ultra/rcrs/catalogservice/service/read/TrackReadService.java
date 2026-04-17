package org.ultra.rcrs.catalogservice.service.read;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackStandaloneDto;
import org.ultra.rcrs.catalogservice.model.read.TrackView;
import org.ultra.rcrs.catalogservice.repository.read.OtherArtistViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.TrackViewRepository;
import org.ultra.rcrs.catalogservice.service.TrackConverter;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackReadService {

    private final TrackViewRepository trackViewRepository;
    private final TrackConverter trackConverter;
    private final OtherArtistViewRepository otherArtistViewRepository;

    @Cacheable("tracks")
    public Mono<TrackFullDto> getTrack(UUID trackId, List<EntityStatus> statuses) {
        return trackViewRepository.findByIdAndStatusIn(trackId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Track", trackId)))
                .zipWith(otherArtistViewRepository.findAllByTrackId(trackId).map(OtherArtistDto::new).collectList())
                .map(tuple -> {
                    var track = tuple.getT1();
                    var others = tuple.getT2();
                    return trackConverter.toFullDto(track, others);
                });
    }

    public Mono<List<TrackStandaloneDto>> getTracks(List<UUID> ids, List<EntityStatus> statuses) {
        return trackViewRepository.findAllByIdAndStatusIn(ids, statuses)
                .collect(Collectors.toMap(TrackView::getId, Function.identity()))
                .map(m -> ids.stream()
                        .map(m::get)
                        .filter(Objects::nonNull)
                        .toList())
                .map(l -> l.stream().map(trackConverter::toStandaloneDto).toList());
    }

}
