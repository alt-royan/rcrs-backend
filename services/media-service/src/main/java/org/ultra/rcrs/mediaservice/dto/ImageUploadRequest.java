package org.ultra.rcrs.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload for uploading an image directly.")
public class ImageUploadRequest {

    @Schema(description = "Encoded image data (e.g. base64) to decode and store")
    @NotNull
    private String image;
}
