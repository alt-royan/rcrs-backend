package org.ultra.rcrs.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Result of a direct image upload.")
public class ImageResponse {
    @Schema(description = "Storage URI/URL where the uploaded image can be retrieved from")
    private String uri;
}
