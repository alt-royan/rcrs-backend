package org.ultra.rcrs.playlistservice.mapper;

import org.ultra.rcrs.playlistservice.dto.response.PlaylistTrackViewDto;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistViewDto;
import org.ultra.rcrs.playlistservice.model.PlaylistDocument;
import org.ultra.rcrs.playlistservice.model.PlaylistTrackDocument;
import org.ultra.rcrs.utils.S3Utils;

public final class PlaylistMapper {

    private PlaylistMapper() {
    }

    public static PlaylistViewDto toViewDto(PlaylistDocument doc, S3Utils s3Utils) {
        return PlaylistViewDto.builder()
                .id(doc.getId())
                .ownerId(doc.getOwnerId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .coverUrl(s3Utils.parseUrl(doc.getCoverS3Key()))
                .isPublic(doc.getIsPublic())
                .trackCount(doc.getTrackCount())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    public static PlaylistTrackViewDto toTrackViewDto(PlaylistTrackDocument doc) {
        return PlaylistTrackViewDto.builder()
                .trackId(doc.getTrackId())
                .position(doc.getPosition())
                .addedAt(doc.getAddedAt())
                .build();
    }
}
