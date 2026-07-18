package org.ultra.rcrs.catalogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.EntityStatus;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistAdminViewDto {

    private String id;
    private String name;
    private String avatarUrl;
    private List<SocialLinkEmbed> socialLinks;
    private List<String> tags;
    private EntityStatus availabilityStatus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SocialLinkEmbed {
        private String resourceName;
        private String url;
    }
}
