package org.ultra.rcrs.catalogservice.model;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;

@Data
public class SocialLink {

    @Column("resource_name")
    private String resourceName;

    @Column("url")
    private String url;

    public SocialLink(SocialLinkDto socialLinkDto) {
        this.resourceName = socialLinkDto.getResourceName();
        this.url = socialLinkDto.getUrl();
    }
}
