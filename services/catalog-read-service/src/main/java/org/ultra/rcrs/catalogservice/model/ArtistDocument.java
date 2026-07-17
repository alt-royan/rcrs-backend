package org.ultra.rcrs.catalogservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "artists")
public class ArtistDocument {

    @Id
    private String id;
    private String name;
    private String avatarUrl;
    private List<SocialLinkEmbed> socialLinks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SocialLinkEmbed {
        private String resourceName;
        private String url;
    }
}
