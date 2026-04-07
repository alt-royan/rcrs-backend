package org.ultra.rcrs.catalogservice.model;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class SocialLink {

    @Column("resource_name")
    private String resourceName;

    @Column("url")
    private String url;
}
