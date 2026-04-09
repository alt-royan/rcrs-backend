package org.ultra.rcrs.catalogservice.model.write;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.ultra.rcrs.catalogservice.dto.ArtistOtherDto;
import org.ultra.rcrs.catalogservice.model.SocialLink;
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

    @Embedded.Empty
    private List<ArtistOther> others;

    @Data
    public static class ArtistOther {

        @Column("name")
        private String name;

        @Column("social_link")
        private SocialLink socialLink;

        @Column("roles")
        @Embedded.Empty
        private Set<ArtistRole> roles;

        public ArtistOther(ArtistOtherDto artistOtherDto) {
            this.name = artistOtherDto.getName();
            this.socialLink = new SocialLink(artistOtherDto.getSocialLink());
            this.roles = artistOtherDto.getRoles();
        }
    }

}