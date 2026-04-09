package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import org.ultra.rcrs.catalogservice.model.SocialLink;

@Data
public class SocialLinkDto {

    private String resourceName;
    private String url;

    public SocialLinkDto(SocialLink socialLink) {
        this.resourceName = socialLink.getResourceName();
        this.url = socialLink.getUrl();
    }

}