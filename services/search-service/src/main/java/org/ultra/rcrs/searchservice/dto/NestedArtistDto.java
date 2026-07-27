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
@Schema(description = "A minimal artist reference embedded inside an album or track search result.")
public class NestedArtistDto {
    @Schema(description = "Short (Base62) public identifier of the artist.")
    private String id;
    @Schema(description = "Display name of the artist.")
    private String name;
    @Schema(description = "URLs of the artist's avatar/profile image, keyed by thumbnail size (SM/MD/LG); empty if none is set.")
    private Map<ImageSize, URI> avatar;
}
