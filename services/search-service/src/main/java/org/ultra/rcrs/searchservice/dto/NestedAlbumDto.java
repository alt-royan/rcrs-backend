package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A minimal album reference embedded inside an artist or track search result.")
public class NestedAlbumDto {
    @Schema(description = "Short (Base62) public identifier of the album.")
    private String id;
    @Schema(description = "Title of the album.")
    private String title;
    @Schema(description = "URL of the album cover art, if one is set.")
    private String coverUrl;
}
