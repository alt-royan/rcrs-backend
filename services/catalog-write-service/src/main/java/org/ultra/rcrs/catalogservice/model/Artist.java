package org.ultra.rcrs.catalogservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.catalogservice.model.converter.SocialLinksConverter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artists")
public class Artist {

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
