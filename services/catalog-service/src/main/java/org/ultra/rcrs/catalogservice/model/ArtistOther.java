package org.ultra.rcrs.catalogservice.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.Set;

@Data
@UserDefinedType("artist_other")
public class ArtistOther {

    @Column("name")
    private String name;

    @Column("social_link")
    private String socialLink;

    @Column("roles")
    private Set<ArtistRole> roles;
}
