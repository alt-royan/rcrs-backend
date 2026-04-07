package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumSimpleDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnTrackDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.repository.OthersOnTrackRepository;
import org.ultra.rcrs.catalogservice.repository.TrackViewRepository;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackCrudService {

    private final TrackViewRepository trackRepository;
    private final OthersOnTrackRepository othersOnTrackRepository;
    private final S3Utils s3Utils;

    public Mono<TrackFullDto> getTrack(UUID trackId, List<EntityStatus> statuses) {
        return trackRepository.findByIdAndStatusIn(trackId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")))
                .zipWith(othersOnTrackRepository.findByTrackId(trackId))
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
                            .artists(track.getArtists().stream()
                                    .map(a -> ArtistOnTrackDto.builder()
                                            .id(Url62.encode(a.getId()))
                                            .name(a.getName())
                                            .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                            .role(a.getRole()).build()).toList())
                            .others(others.getOthers())
                            .build();
                });
    }

/*    public Mono<List<TrackInAlbumDto>> getTracksForAlbum(UUID albumId, List<EntityStatus> statuses) {
        return trackConverter.collectTracksForAlbum(albumId, statuses);
    }*/

}
