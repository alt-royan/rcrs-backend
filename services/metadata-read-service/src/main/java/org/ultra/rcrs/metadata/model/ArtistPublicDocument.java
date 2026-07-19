package org.ultra.rcrs.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ultra.rcrs.enums.EntityStatus;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "artists")
public class ArtistPublicDocument {

    @Id
    private String id;
    private String name;
    private String avatarS3Key;
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
