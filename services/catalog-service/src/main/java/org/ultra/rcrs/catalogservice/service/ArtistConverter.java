package org.ultra.rcrs.catalogservice.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnAlbumDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnTrackDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistStandaloneDto;
import org.ultra.rcrs.catalogservice.model.read.*;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistConverter {

    private final S3Utils s3Utils;

    public List<ArtistOnAlbumDto> onAlbumToDto(@Nonnull List<ArtistOnAlbumView> artists) {
        Objects.requireNonNull(artists, "artists must not be null");
        return artists.stream()
                .map(a -> ArtistOnAlbumDto.builder()
                        .id(Url62.encode(a.getId()))
                        .name(a.getName())
                        .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole()).build()).toList();
    }

    public List<ArtistOnTrackDto> onTackToDto(@Nonnull List<ArtistOnTrackView> artists) {
        Objects.requireNonNull(artists, "artists must not be null");
        return artists.stream()
                .map(a -> ArtistOnTrackDto.builder()
                        .id(Url62.encode(a.getId()))
                        .name(a.getName())
                        .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                        .role(a.getRole()).build()).toList();
    }

    public ArtistDto toDto(@Nonnull ArtistView artist) {
        Objects.requireNonNull(artist, "artist must not be null");
        return ArtistDto.builder()
                .id(Url62.encode(artist.getId()))
                .name(artist.getName())
                .avatarUrl(s3Utils.parseUrl(artist.getAvatarS3Key()))
                .socialLinks(artist.getSocialLinks().getItems().stream().map(SocialLinkDto::new).toList())
                .build();
    }

    public ArtistStandaloneDto toStandaloneDto(@Nonnull ArtistView artist) {
        Objects.requireNonNull(artist, "artist must not be null");
        return ArtistStandaloneDto.builder()
                .id(Url62.encode(artist.getId()))
                .name(artist.getName())
                .avatarUrl(s3Utils.parseUrl(artist.getAvatarS3Key()))
                .build();
    }

    public Map<String, Object> toIndex(ArtistView artist, List<ArtistTrackView> tracks, List<ArtistAlbumView> albums) {
        var tr = tracks.stream().map(t -> Map.of("id", Url62.encode(t.getTrackId()), "title", t.getTitle())).toList();
        var al = albums.stream().map(a -> Map.of("id", Url62.encode(a.getAlbumId()), "title", a.getTitle())).toList();
        Map<String, Object> index = new HashMap<>();
        index.put("id", Url62.encode(artist.getId()));
        index.put("name", artist.getName());
        index.put("tracks", tr);
        index.put("albums", al);
        return index;
    }

}
