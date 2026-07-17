package org.ultra.rcrs.catalogservice.model.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artist_album_view")
public class ArtistTrackView {

    @Column("artist_id")
    private UUID artistId;

    @Column("artist_role")
    private ArtistRole artistRole;

    @Column("track_id")
    private UUID trackId;

    private LifecycleStatus status;

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

    private AlbumView album;

    private List<ArtistOnTrackView> artists;
}