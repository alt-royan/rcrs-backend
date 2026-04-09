package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.kafka.producer.EventProducer;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.model.write.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.repository.*;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumCrudService {

    private final AlbumViewRepository albumViewRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final TrackInAlbumViewRepository trackInAlbumViewRepository;
    private final ArtistConverter artistConverter;
    private final TrackCrudService trackCrudService;
    private final S3Utils s3Utils;
    private final EventProducer eventProducer;

    public Mono<AlbumFullDto> getAlbum(UUID albumId, List<EntityStatus> statuses) {
        return albumViewRepository.findByIdAndStatusIn(albumId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .zipWith(getTracksInAlbum(albumId, statuses))
                .map(tuple -> {
                    var album = tuple.getT1();
                    var tracks = tuple.getT2();
                    return AlbumFullDto.builder()
                            .id(Url62.encode(album.getId()))
                            .status(album.getStatus())
                            .title(album.getTitle())
                            .type(album.getType())
                            .releaseDate(album.getReleaseDate())
                            .year(album.getYear())
                            .totalTracks(album.getTotalTracks())
                            .totalDurationMs(album.getTotalDurationMs())
                            .coverUrl(s3Utils.parseUrl(album.getCoverS3Key()))
                            .explicit(album.getExplicit())
                            .available(album.getAvailable())
                            .artists(artistConverter.onAlbumToDto(album.getArtists()))
                            .tracks(tracks)
                            .build();
                });
    }

    public Mono<List<TrackInAlbumDto>> getTracksInAlbum(UUID albumId, List<EntityStatus> statuses) {
        return trackInAlbumViewRepository.findAllByAlbumId(albumId, statuses)
                .map(t -> TrackInAlbumDto.builder()
                        .id(Url62.encode(t.getId()))
                        .status(t.getStatus())
                        .title(t.getTitle())
                        .releaseDate(t.getReleaseDate())
                        .durationMs(t.getDurationMs())
                        .trackNumber(t.getTrackNumber())
                        .explicit(t.getExplicit())
                        .available(t.getAvailable())
                        .artists(artistConverter.onTackToDto(t.getArtists()))
                        .build()).collectList();
    }

    /*public Mono<List<AlbumStandalone>> getAlbumsForArtist(UUID artistId, List<EntityStatus> statuses, ArtistRole[] roles, AlbumType[] types, Sort.Direction direction) {
        return albumByArtistRepository.findAll(artistId, statuses, roles, types, direction)
                .flatMap(albumConverter::toDto)
                .collectList();
    }*/

    @Transactional
    public Mono<Void> createAlbum(AlbumUploadRequest request) {
        if (request.getTracks() != null && !request.getTracks().isEmpty()) {
            Set<Integer> numbers = request.getTracks().stream()
                    .map(TrackUploadRequest::getTrackNumber)
                    .collect(Collectors.toSet());

            int size = request.getTracks().size();

            if (numbers.size() != size || !IntStream.rangeClosed(1, size).allMatch(numbers::contains)) {
                throw new BadRequestException("trackNumbers are wrong");
            }
        }

        UUID albumId = UUID.randomUUID();
        return checkArtists(request.getArtists())
                .then(albumRepository.save(Album.builder()
                                .id(albumId)
                                .status(EntityStatus.CREATED)
                                .title(request.getTitle())
                                .type(request.getType())
                                .releaseDate(request.getReleaseDate())
                                .coverS3Key(s3Utils.parseKey(request.getCoverUri()))
                                .available(true)
                                .build()).doOnSuccess(v -> log.info("Album {} saved with id {}", request.getTitle(), albumId))
                        .thenMany(saveArtistsToAlbum(request.getArtists(), albumId))
                        .thenMany(trackCrudService.saveTracks(request.getTracks(), albumId))
                        .then(Mono.fromRunnable(() -> eventProducer.albumCreated(albumId))));
    }

    private Flux<Void> checkArtists(List<ArtistIdDto> artists) {
        return Flux.fromIterable(artists)
                .flatMap(a -> {
                    var id = Url62.decode(a.getId());
                    return artistRepository.existsById(id)
                            .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + id + " was not found")))
                            .then();
                });
    }

    private Flux<ArtistToAlbum> saveArtistsToAlbum(List<ArtistIdDto> artists, UUID albumId) {
        return Flux.fromIterable(artists)
                .map(a -> ArtistToAlbum.builder()
                        .id(new ArtistToAlbum.ArtistToAlbumId(Url62.decode(a.getId()), albumId))
                        .artistRole(a.getRole())
                        .build())
                .flatMap(a -> artistToAlbumRepository.save(a).doOnSuccess(v ->
                        log.info("Artist {} with role {} attached to album {}", a.getId(), a.getArtistRole(), albumId)));
    }

}
