package org.ultra.rcrs.catalogservice.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumOfArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumSimpleDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumStandaloneDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.model.read.AlbumView;
import org.ultra.rcrs.catalogservice.model.read.ArtistAlbumView;
import org.ultra.rcrs.catalogservice.model.read.TrackWithoutAlbumView;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumConverter {

    private final S3Utils s3Utils;
    private final ArtistConverter artistConverter;

    public AlbumFullDto toFullDto(@Nonnull AlbumView album, @Nonnull List<TrackInAlbumDto> tracks) {
        Objects.requireNonNull(album, "album must not be null");
        Objects.requireNonNull(tracks, "tracks must not be null");

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
    }

    public AlbumStandaloneDto toStandaloneDto(@Nonnull AlbumView album) {
        Objects.requireNonNull(album, "album must not be null");

        return AlbumStandaloneDto.builder()
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
                .build();
    }

    public AlbumSimpleDto toSimpleDto(@Nonnull AlbumView album) {
        Objects.requireNonNull(album, "album must not be null");

        return AlbumSimpleDto.builder()
                .id(Url62.encode(album.getId()))
                .title(album.getTitle())
                .coverUrl(s3Utils.parseUrl(album.getCoverS3Key())).build();
    }

    public AlbumOfArtistDto toOfArtistDto(@Nonnull ArtistAlbumView album) {
        Objects.requireNonNull(album, "album must not be null");

        return AlbumOfArtistDto.builder()
                .id(Url62.encode(album.getAlbumId()))
                .artistRole(album.getArtistRole())
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
                .build();
    }

    public Map<String, Object> toIndex(AlbumView album, List<TrackWithoutAlbumView> tracks) {
        var artists = album.getArtists().stream()
                .map(a -> Map.of("id", Url62.encode(a.getId()), "name", a.getName()))
                .toList();
        var tr = tracks.stream().map(t -> Map.of("id", Url62.encode(t.getId()), "title", t.getTitle()))
                .toList();
        Map<String, Object> index = new HashMap<>();
        index.put("id", Url62.encode(album.getId()));
        index.put("title", album.getTitle());
        index.put("year", album.getYear());
        index.put("published", album.getStatus() == EntityStatus.PUBLISHED);
        index.put("tracks", tr);
        index.put("artists", artists);
        return index;
    }


}
