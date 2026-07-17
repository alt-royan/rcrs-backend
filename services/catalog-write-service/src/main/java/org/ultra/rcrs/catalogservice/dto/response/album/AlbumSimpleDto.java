package org.ultra.rcrs.catalogservice.dto.response.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AlbumSimpleDto {

    private String id;
    private String title;
    private String coverUrl;
}
