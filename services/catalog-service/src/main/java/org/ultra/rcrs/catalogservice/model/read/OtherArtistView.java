package org.ultra.rcrs.catalogservice.model.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.catalogservice.model.SocialLinks;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("other_artist_view")
public class OtherArtistView {

    @Column("track_id")
    private UUID trackId;

    @Column("name")
    private String name;

    @Column("roles")
    private Set<ArtistRole> roles;

    @Column("social_links")
    private SocialLinks socialLinks;
}