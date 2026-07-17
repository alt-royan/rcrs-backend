package org.ultra.rcrs.catalogservice.dto.response.artist;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArtistStandaloneDto {

    private String id;
    private String name;
    private String avatarUrl;
}
