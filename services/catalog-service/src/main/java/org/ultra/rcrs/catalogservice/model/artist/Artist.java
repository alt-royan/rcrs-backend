package org.ultra.rcrs.catalogservice.model.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.catalogservice.model.SocialLink;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artists_by_id")
public class Artist {

    @PrimaryKey
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("social_links")
    private List<SocialLink> socialLinks;

    @Column("avatar_s3_key")
    private String avatarKey;
}