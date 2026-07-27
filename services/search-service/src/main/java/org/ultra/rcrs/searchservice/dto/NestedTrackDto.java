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
@Schema(description = "A minimal track reference embedded inside an artist or album search result.")
public class NestedTrackDto {
    @Schema(description = "Short (Base62) public identifier of the track.")
    private String id;
    @Schema(description = "Title of the track.")
    private String title;
}
