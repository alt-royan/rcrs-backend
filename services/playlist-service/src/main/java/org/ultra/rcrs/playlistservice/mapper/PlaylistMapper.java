package org.ultra.rcrs.playlistservice.mapper;

import org.ultra.rcrs.playlistservice.dto.response.PlaylistTrackViewDto;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistViewDto;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.utils.ImageUtils;

public final class PlaylistMapper {

    private PlaylistMapper() {
    }

    public static PlaylistViewDto toViewDto(Playlist playlist, ImageUtils imageUtils) {
        return PlaylistViewDto.builder()
                .id(playlist.getId())
                .ownerId(playlist.getOwnerId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .tags(playlist.getTags())
                .coverUrl(imageUtils.parseUrl(playlist.getCoverS3Key()))
                .isPrivate(playlist.getIsPrivate())
                .type(playlist.getType())
                .trackCount(playlist.getTrackCount())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .build();
    }

    public static PlaylistTrackViewDto toTrackViewDto(PlaylistTrack track) {
        return PlaylistTrackViewDto.builder()
                .trackId(track.getTrackId())
                .position(track.getPosition())
                .addedAt(track.getAddedAt())
                .build();
    }
}
