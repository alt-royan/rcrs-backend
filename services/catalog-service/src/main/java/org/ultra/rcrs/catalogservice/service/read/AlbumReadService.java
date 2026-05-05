package org.ultra.rcrs.catalogservice.service.read;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumStandaloneDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.model.read.AlbumView;
import org.ultra.rcrs.catalogservice.repository.read.AlbumViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.TrackWithoutAlbumViewRepository;
import org.ultra.rcrs.catalogservice.service.AlbumConverter;
import org.ultra.rcrs.catalogservice.service.TrackConverter;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumReadService {

    private final AlbumViewRepository albumViewRepository;
    private final TrackWithoutAlbumViewRepository trackWithoutAlbumViewRepository;
    private final AlbumConverter albumConverter;
    private final TrackConverter trackConverter;

    @Cacheable("albums")
    public Mono<AlbumFullDto> getAlbum(UUID albumId, List<EntityStatus> statuses) {
        return albumViewRepository.findByIdAndStatusIn(albumId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Album", albumId)))
                .zipWith(getTracksInAlbum(albumId, statuses))
                .map(tuple -> {
                    var album = tuple.getT1();
                    var tracks = tuple.getT2();
                    return albumConverter.toFullDto(album, tracks);
                });
    }

    public Mono<List<AlbumStandaloneDto>> getAlbums(List<UUID> ids, List<EntityStatus> statuses) {
        return albumViewRepository.findAllByIdAndStatusIn(ids, statuses)
                .collect(Collectors.toMap(AlbumView::getId, Function.identity()))
                .map(m -> ids.stream()
                        .map(m::get)
                        .toList())
                .map(l -> l.stream().map(albumConverter::toStandaloneDto).toList());
    }

    public Mono<List<TrackInAlbumDto>> getTracksInAlbum(UUID albumId, List<EntityStatus> statuses) {
        return trackWithoutAlbumViewRepository.findAllByAlbumIdAndStatusId(albumId, statuses)
                .map(trackConverter::toTrackInAlbumDto).collectList();
    }

}
