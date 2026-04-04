package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.enums.TrackStatus;
import org.ultra.rcrs.utils.Url62;

@Data
public abstract class TrackMetadataAbstract {

    private String id;

    private TrackStatus status;

    private String title;

    private Long durationMs;

    private Integer trackNumber;

    private Boolean explicit;

    private Boolean available;

    protected TrackMetadataAbstract(Track track) {
        this.id = Url62.encode(track.getKey().getId());
        this.status = track.getKey().getStatus();
        this.title = track.getTitle();
        this.durationMs = track.getDurationMs();
        this.trackNumber = track.getTrackNumber();
        this.explicit = track.getExplicit();
        this.available = track.getAvailable();
    }

}