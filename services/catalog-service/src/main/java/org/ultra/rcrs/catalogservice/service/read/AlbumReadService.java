package org.ultra.rcrs.catalogservice.service.read;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumOfArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.repository.read.AlbumViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.ArtistAlbumViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.TrackViewRepository;
import org.ultra.rcrs.catalogservice.service.ArtistConverter;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumReadService {

    private final AlbumViewRepository albumViewRepository;
    private final TrackViewRepository trackViewRepository;
    private final ArtistConverter artistConverter;
    private final S3Utils s3Utils;

    @Cacheable("albums")
    public Mono<AlbumFullDto> getAlbum(UUID albumId, List<EntityStatus> statuses) {
        return albumViewRepository.findByIdAndStatusIn(albumId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Album", albumId)))
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
        return trackViewRepository.findAllByAlbumIdAndStatusId(albumId, statuses)
                .map(t -> TrackInAlbumDto.builder()
                        .id(Url62.encode(t.getId()))
                        .status(t.getStatus())
                        .title(t.getTitle())
                        .durationMs(t.getDurationMs())
                        .trackNumber(t.getTrackNumber())
                        .explicit(t.getExplicit())
                        .available(t.getAvailable())
                        .artists(artistConverter.onTackToDto(t.getArtists()))
                        .build()).collectList();
    }

}
