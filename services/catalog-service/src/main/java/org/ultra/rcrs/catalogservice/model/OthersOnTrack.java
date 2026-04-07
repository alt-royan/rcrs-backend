package org.ultra.rcrs.catalogservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OthersOnTrack {

    @Id
    @Column("track_id")
    private UUID trackId;

    private List<ArtistOther> others;

    @Data
    public static class ArtistOther {

        @Column("name")
        private String name;

        @Column("social_link")
        private SocialLink socialLink;

        @Column("roles")
        private Set<ArtistRole> roles;
    }

}