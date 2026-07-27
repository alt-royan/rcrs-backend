package org.ultra.rcrs.playlistservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistStandaloneDto;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistTrackViewDto;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistViewDto;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.model.PlaylistWithCountProjection;
import org.ultra.rcrs.utils.ImageUtils;
import org.ultra.rcrs.utils.Url62;

@RequiredArgsConstructor
@Component
public class PlaylistMapper {

    private final ImageUtils imageUtils;

    public PlaylistViewDto toViewDto(PlaylistWithCountProjection playlist) {
        return PlaylistViewDto.builder()
                .id(Url62.encode(playlist.id()))
                .ownerId(playlist.ownerId())
                .title(playlist.title())
                .description(playlist.description())
                .tags(playlist.tags())
                .cover(imageUtils.parseUrls(playlist.coverS3Key()))
                .isPrivate(playlist.isPrivate())
                .type(playlist.type())
                .trackCount(playlist.trackCount())
                .createdAt(playlist.createdAt())
                .updatedAt(playlist.updatedAt())
                .build();
    }

    public PlaylistStandaloneDto toStandaloneDto(Playlist playlist) {
        return PlaylistStandaloneDto.builder()
                .id(Url62.encode(playlist.getId()))
                .ownerId(playlist.getOwnerId())
                .title(playlist.getTitle())
                .cover(imageUtils.parseUrls(playlist.getCoverS3Key()))
                .isPrivate(playlist.getIsPrivate())
                .type(playlist.getType())
                .build();
    }

    public PlaylistTrackViewDto toTrackViewDto(PlaylistTrack track) {
        return PlaylistTrackViewDto.builder()
                .trackId(track.getTrackId())
                .position(track.getPosition())
                .addedAt(track.getAddedAt())
                .build();
    }
}
