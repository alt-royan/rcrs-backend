package org.ultra.rcrs.catalogservice.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@Data
@UserDefinedType("social_link")
public class SocialLink {

    @Column("resource_name")
    private String resourceName;

    @Column("url")
    private String url;
}
