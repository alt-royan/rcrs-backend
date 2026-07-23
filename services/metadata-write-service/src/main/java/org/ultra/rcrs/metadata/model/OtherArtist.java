package org.ultra.rcrs.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.metadata.dto.OtherArtistDto;
import org.ultra.rcrs.metadata.model.converter.ArtistRolesConverter;
import org.ultra.rcrs.metadata.model.converter.SocialLinksConverter;

import java.util.Set;
import java.util.UUID;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "other_artist")
public class OtherArtist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "track_id", nullable = false)
    private UUID trackId;

    @Column(name = "name")
    private String name;

    @Column(name = "roles")
    @Convert(converter = ArtistRolesConverter.class)
    private Set<ArtistRole> roles;

    @Column(name = "social_links")
    @Convert(converter = SocialLinksConverter.class)
    private SocialLinks socialLinks;

    public OtherArtist(OtherArtistDto otherArtistDto, UUID trackId) {
        this.trackId = trackId;
        this.name = otherArtistDto.getName();
        this.roles = otherArtistDto.getRoles();
        if (!otherArtistDto.getSocialLinks().isEmpty()) {
            this.socialLinks = new SocialLinks(otherArtistDto.getSocialLinks().getFirst());
        }
    }
}
