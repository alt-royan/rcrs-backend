package org.ultra.rcrs.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.catalogservice.model.SocialLinks;

import java.net.URI;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialLinkDto {

    @NotBlank
    private String resourceName;

    @NotNull
    private URI url;

    public SocialLinkDto(SocialLinks.Link link) {
        this.resourceName = link.getResourceName();
        this.url = URI.create(link.getUrl());
    }
}
