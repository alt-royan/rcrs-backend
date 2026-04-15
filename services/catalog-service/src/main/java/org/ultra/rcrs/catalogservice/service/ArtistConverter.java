package org.ultra.rcrs.catalogservice.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnAlbumDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnTrackDto;
import org.ultra.rcrs.catalogservice.model.read.ArtistOnAlbumView;
import org.ultra.rcrs.catalogservice.model.read.ArtistOnTrackView;
import org.ultra.rcrs.catalogservice.model.read.ArtistView;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
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

}
