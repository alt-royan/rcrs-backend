package org.ultra.rcrs.catalogservice.model.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("track_without_album_view")
public class TrackWithoutAlbumView {

    private UUID id;

    private EntityStatus status;

    private String title;

    @Column("release_date")
    private LocalDate releaseDate;

    @Column("duration_ms")
    private Integer durationMs;

    @Column("track_number")
    private Integer trackNumber;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    @Column("album_id")
    private UUID albumId;

    private List<ArtistOnTrackView> artists;

}