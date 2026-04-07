package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.repository.AlbumViewRepository;
import org.ultra.rcrs.catalogservice.repository.TrackInAlbumViewRepository;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumCrudService {

    private final AlbumViewRepository albumRepository;
    private final TrackInAlbumViewRepository trackInAlbumViewRepository;
    private final ArtistConverter artistConverter;
    private final S3Utils s3Utils;

    public Mono<AlbumFullDto> getAlbum(UUID albumId, List<EntityStatus> statuses) {
        return albumRepository.findByIdAndStatusIn(albumId, statuses)
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

  /*  public Mono<List<AlbumStandalone>> getAlbumsForArtist(UUID artistId, List<EntityStatus> statuses, ArtistRole[] roles, AlbumType[] types, Sort.Direction direction) {
        return albumByArtistRepository.findAll(artistId, statuses, roles, types, direction)
                .flatMap(albumConverter::toDto)
                .collectList();
    }

    public Mono<AlbumFullDto> createAlbum(AlbumCreateRequest request) {
        Set<Integer> numbers = request.getTracks().stream()
                .map(TrackCreateRequest::getTrackNumber)
                .collect(Collectors.toSet());

        if (numbers.size() != request.getTracks().size()) {
            throw new BadRequestException("trackNumbers are wrong");
        }
        int size = request.getTracks().size();

        for (int i = 1; i <= size; i++) {
            if (!numbers.contains(i)) {
                throw new BadRequestException("trackNumbers are wrong");
            }
        }

        return albumRepository.save(Album.builder()
                .title(request.getTitle())
                .type(request.getType())
                .releaseDate(request.getReleaseDate())
                .coverS3Key(S3Utils.parseKey(request.getCoverUri()))
                .explicit(request.getExplicit())
                .mainArtists(request.getArtists().stream()
                        .map(dto -> Url62.decode(dto.getId()))
                        .collect(Collectors.toSet()))
                .build()
        ).flatMap(album -> Flux.fromIterable(request.getTracks())
                .map(dto -> {
                    Set<UUID> mainArtists = dto.getArtists().stream()
                            .filter(a -> ArtistRole.MAIN_ARTIST.equals(a.getRole()))
                            .map(a -> Url62.decode(a.getId())).collect(Collectors.toSet());
                    Set<UUID> featuredArtists = dto.getArtists().stream()
                            .filter(a -> ArtistRole.FEATURED_ARTIST.equals(a.getRole()))
                            .map(a -> Url62.decode(a.getId())).collect(Collectors.toSet());

                    return Track.builder()
                            .albumId(album.getKey().getId())
                            .title(dto.getTitle())
                            .releaseDate(album.getReleaseDate())
                            .trackNumber(dto.getTrackNumber())
                            .explicit(dto.getExplicit())
                            .mainArtists(mainArtists)
                            .featuredArtists(featuredArtists)
                            .others(dto.getOthers())
                            .build();
                }).flatMap(t -> trackRepository.save(t).then(incTotalTracks(album.getKey().getId())))
                .then(getAlbum(album.getKey().getId(), List.of(EntityStatus.values()))));

    }

    public Mono<Void> incTotalTracks(UUID albumId) {
        return albumRepository.findById(albumId).flatMap(a -> {
            var totalTracks = a.getTotalTracks() + 1;
            var statuses = EntityStatus.values();
            return albumRepository.updateTotalTracks(albumId, List.of(statuses), totalTracks).then(albumByArtistRepository.updateTotalTracks(albumId, statuses, totalTracks));
        });
    }*/


}
