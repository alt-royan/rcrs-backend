package org.ultra.rcrs.catalogservice.dto.response.artist;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArtistSimple {

    private String id;

    private String name;

    private String avatarUrl;
}