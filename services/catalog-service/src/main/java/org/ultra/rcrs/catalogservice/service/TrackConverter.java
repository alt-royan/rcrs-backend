package org.ultra.rcrs.catalogservice.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackStandaloneDto;
import org.ultra.rcrs.catalogservice.model.read.TrackView;
import org.ultra.rcrs.catalogservice.model.read.TrackWithoutAlbumView;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackConverter {

    private final ArtistConverter artistConverter;
    private final AlbumConverter albumConverter;

    public TrackFullDto toFullDto(@Nonnull TrackView track, List<OtherArtistDto> others) {
        Objects.requireNonNull(track, "track must not be null");

        return TrackFullDto.builder()
                .id(Url62.encode(track.getId()))
                .status(track.getStatus())
                .title(track.getTitle())
                .releaseDate(track.getReleaseDate())
                .durationMs(track.getDurationMs())
                .trackNumber(track.getTrackNumber())
                .explicit(track.getExplicit())
                .available(track.getAvailable())
                .album(albumConverter.toSimpleDto(track.getAlbum()))
                .artists(artistConverter.onTackToDto(track.getArtists()))
                .others(others)
                .build();
    }

    public TrackStandaloneDto toStandaloneDto(@Nonnull TrackView track) {
        Objects.requireNonNull(track, "track must not be null");

        return TrackStandaloneDto.builder()
                .id(Url62.encode(track.getId()))
                .status(track.getStatus())
                .title(track.getTitle())
                .releaseDate(track.getReleaseDate())
                .durationMs(track.getDurationMs())
                .trackNumber(track.getTrackNumber())
                .explicit(track.getExplicit())
                .available(track.getAvailable())
                .album(albumConverter.toSimpleDto(track.getAlbum()))
                .artists(artistConverter.onTackToDto(track.getArtists()))
                .build();
    }

    public TrackInAlbumDto toTrackInAlbumDto(@Nonnull TrackWithoutAlbumView track) {
        Objects.requireNonNull(track, "track must not be null");

        return TrackInAlbumDto.builder()
                .id(Url62.encode(track.getId()))
                .status(track.getStatus())
                .title(track.getTitle())
                .durationMs(track.getDurationMs())
                .trackNumber(track.getTrackNumber())
                .explicit(track.getExplicit())
                .available(track.getAvailable())
                .artists(artistConverter.onTackToDto(track.getArtists()))
                .build();
    }

    public Map<String, Object> toIndex(TrackView track) {
        var artists = track.getArtists().stream()
                .map(a -> Map.of("id", Url62.encode(a.getId()), "name", a.getName()))
                .toList();
        var album = Map.of("id", Url62.encode(track.getAlbum().getId()), "title", track.getAlbum().getTitle());
        Map<String, Object> index = new HashMap<>();
        index.put("id", Url62.encode(track.getId()));
        index.put("title", track.getTitle());
        index.put("published", track.getStatus() == EntityStatus.PUBLISHED);
        index.put("album", album);
        index.put("artists", artists);
        return index;
    }


}
