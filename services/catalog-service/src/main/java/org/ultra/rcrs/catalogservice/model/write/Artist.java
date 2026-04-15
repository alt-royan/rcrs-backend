package org.ultra.rcrs.catalogservice.model.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.catalogservice.model.SocialLinks;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artists")
public class Artist {

    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("avatar_s3_key")
    private String avatarS3Key;

    @Column("social_links")
    private SocialLinks socialLinks;
}