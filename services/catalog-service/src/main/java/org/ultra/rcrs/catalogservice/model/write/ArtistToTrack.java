package org.ultra.rcrs.catalogservice.model.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artist_to_track")
public class ArtistToTrack {

    @Column("artist_id")
    private UUID artistId;

    @Column("track_id")
    private UUID trackId;

    @Column("artist_role")
    private ArtistRole artistRole;

}
