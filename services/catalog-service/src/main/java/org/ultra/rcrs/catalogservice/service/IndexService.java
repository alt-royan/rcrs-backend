package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.catalogservice.repository.read.*;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.kafka.events.IndexEntityEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class IndexService {

    private final ObjectMapper objectMapper;
    private final ArtistViewRepository artistViewRepository;
    private final AlbumViewRepository albumViewRepository;
    private final TrackViewRepository trackViewRepository;
    private final TrackWithoutAlbumViewRepository trackWithoutAlbumViewRepository;
    private final ArtistAlbumViewRepository artistAlbumViewRepository;
    private final ArtistTrackViewRepository artistTrackViewRepository;

    private final ArtistConverter artistConverter;
    private final AlbumConverter albumConverter;
    private final TrackConverter trackConverter;


    public Flux<IndexEntityEvent> createArtistIndexEvents(int batchSize) {
        return artistViewRepository.findAll()
                .flatMap(a -> artistAlbumViewRepository.findAllByArtist(a.getId(), List.of(EntityStatus.PUBLISHED), ArtistRole.MAIN_ARTIST).collectList()
                        .zipWith(artistTrackViewRepository.findAllByArtist(a.getId(), List.of(EntityStatus.PUBLISHED)).collectList())
                        .map(tuple -> {
                            var albums = tuple.getT1();
                            var tracks = tuple.getT2();
                            return artistConverter.toIndex(a, tracks, albums);
                        })
                )
                .buffer(batchSize)
                .map(objectMapper::writeValueAsString)
                .map(json -> new IndexEntityEvent(IndexEntityEvent.ARTIST_CREATE_BATCH, json));
    }

    public Flux<IndexEntityEvent> createAlbumIndexEvents(int batchSize) {
        return albumViewRepository.findAll()
                .flatMap(a -> trackWithoutAlbumViewRepository.findAllByAlbumId(a.getId()).collectList()
                        .map(tracks -> albumConverter.toIndex(a, tracks))
                )
                .buffer(batchSize)
                .map(objectMapper::writeValueAsString)
                .map(json -> new IndexEntityEvent(IndexEntityEvent.ALBUM_CREATE_BATCH, json));
    }

    public Flux<IndexEntityEvent> createTrackIndexEvents(int batchSize) {
        return trackViewRepository.findAll()
                .map(trackConverter::toIndex)
                .buffer(batchSize)
                .map(objectMapper::writeValueAsString)
                .map(json -> new IndexEntityEvent(IndexEntityEvent.TRACK_CREATE_BATCH, json));
    }

    public Mono<IndexEntityEvent> createArtistIndexEvent(UUID artistId) {
        return artistViewRepository.findById(artistId)
                .zipWith(artistAlbumViewRepository.findAllByArtist(artistId, List.of(EntityStatus.PUBLISHED), ArtistRole.MAIN_ARTIST).collectList())
                .zipWith(artistTrackViewRepository.findAllByArtist(artistId, List.of(EntityStatus.PUBLISHED)).collectList())
                .map(tuple -> {
                    var artist = tuple.getT1().getT1();
                    var albums = tuple.getT1().getT2();
                    var tracks = tuple.getT2();

                    return artistConverter.toIndex(artist, tracks, albums);
                })
                .map(objectMapper::writeValueAsString)
                .map(json -> new IndexEntityEvent(IndexEntityEvent.ARTIST_CREATE_SINGLE, json));
    }

    public Mono<IndexEntityEvent> createAlbumIndexEvent(UUID albumId) {
        return albumViewRepository.findById(albumId)
                .zipWith(trackWithoutAlbumViewRepository.findAllByAlbumId(albumId).collectList())
                .map(tuple -> albumConverter.toIndex(tuple.getT1(), tuple.getT2()))
                .map(objectMapper::writeValueAsString)
                .map(json -> new IndexEntityEvent(IndexEntityEvent.ALBUM_CREATE_SINGLE, json));
    }

    public Mono<IndexEntityEvent> createTrackIndexEvent(UUID trackId) {
        return trackViewRepository.findById(trackId)
                .map(trackConverter::toIndex)
                .map(objectMapper::writeValueAsString)
                .map(json -> new IndexEntityEvent(IndexEntityEvent.TRACK_CREATE_SINGLE, json));
    }


}
