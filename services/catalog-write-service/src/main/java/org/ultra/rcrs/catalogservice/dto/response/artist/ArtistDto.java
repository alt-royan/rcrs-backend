package org.ultra.rcrs.catalogservice.dto.response.artist;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.model.SocialLinks;
import org.ultra.rcrs.catalogservice.model.converter.SocialLinksConverter;

import java.util.UUID;

@Data
@Builder
public class ArtistDto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar_s3_key")
    private String avatarS3Key;

    @Column(name = "social_links")
    @Convert(converter = SocialLinksConverter.class)
    private SocialLinks socialLinks;
}
