package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.ImageSize;

import java.net.URI;
import java.util.Map;

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
    @Schema(description = "URLs of the album cover art, keyed by thumbnail size (SM/MD/LG); empty if none is set.")
    private Map<ImageSize, URI> cover;
}
