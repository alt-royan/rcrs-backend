package org.ultra.rcrs.catalogservice.model.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.catalogservice.model.SocialLink;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artists")
public class Artist {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("social_links")
    @Embedded.Empty
    private List<SocialLink> socialLinks;

    @Column("avatar_s3_key")
    private String avatarS3Key;
}