package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.TrackStatus;
import org.ultra.rcrs.utils.Url62;

import java.time.LocalDate;

@Data
public abstract class AlbumMetadataAbstract {

    private String id;

    private TrackStatus status;

    private String title;

    private Long totalDurationMs;

    private AlbumType type;

    private LocalDate releaseDate;

    private String coverUrl;

    private Integer totalTracks;

    private Boolean explicit;

    private Boolean available;

    protected AlbumMetadataAbstract(Album album) {
        this.id = Url62.encode(album.getKey().getId());
        this.status = album.getKey().getStatus();
        this.title = album.getTitle();
        this.totalDurationMs = album.getTotalDurationMs();
        this.type = album.getType();
        this.releaseDate = album.getReleaseDate();
        this.coverUrl = S3Utils.createResourceS3Url(album.getCoverKey());
        this.totalTracks = album.getTotalTracks();
        this.explicit = album.getExplicit();
        this.available = album.getAvailable();
    }

}