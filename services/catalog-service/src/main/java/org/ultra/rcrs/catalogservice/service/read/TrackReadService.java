package org.ultra.rcrs.catalogservice.service.read;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumSimpleDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.repository.read.OtherArtistViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.TrackWithAlbumViewRepository;
import org.ultra.rcrs.catalogservice.service.ArtistConverter;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackReadService {

    private final TrackWithAlbumViewRepository trackWithAlbumViewRepository;
    private final ArtistConverter artistConverter;
    private final OtherArtistViewRepository otherArtistViewRepository;
    private final S3Utils s3Utils;

    @Cacheable("tracks")
    public Mono<TrackFullDto> getTrack(UUID trackId, List<EntityStatus> statuses) {
        return trackWithAlbumViewRepository.findByIdAndStatusIn(trackId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Track", trackId)))
                .zipWith(otherArtistViewRepository.findAllByTrackId(trackId).collectList())
                .map(tuple -> {
                    var track = tuple.getT1();
                    var others = tuple.getT2();
                    return TrackFullDto.builder()
                            .id(Url62.encode(track.getId()))
                            .status(track.getStatus())
                            .title(track.getTitle())
                            .releaseDate(track.getReleaseDate())
                            .durationMs(track.getDurationMs())
                            .trackNumber(track.getTrackNumber())
                            .explicit(track.getExplicit())
                            .available(track.getAvailable())
                            .album(AlbumSimpleDto.builder()
                                    .id(Url62.encode(track.getAlbum().getId()))
                                    .title(track.getAlbum().getTitle())
                                    .coverUrl(s3Utils.parseUrl(track.getAlbum().getCoverS3Key())).build())
                            .artists(artistConverter.onTackToDto(track.getArtists()))
                            .others(others.stream().map(OtherArtistDto::new).toList())
                            .build();
                });
    }

}
