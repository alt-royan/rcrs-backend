package org.ultra.rcrs.catalogservice.dto.response.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.enums.ArtistRole;

@Data
@Builder
@AllArgsConstructor
public class ArtistOnAlbumDto {
    private String id;
    private String name;
    private String avatarUrl;
    private ArtistRole role;
}
