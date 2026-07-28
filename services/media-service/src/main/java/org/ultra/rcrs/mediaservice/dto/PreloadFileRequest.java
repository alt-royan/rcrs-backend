package org.ultra.rcrs.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to register a pending audio upload and obtain a presigned S3 upload URL for it.")
public class PreloadFileRequest {

    @Schema(description = "Original file name of the audio file to be uploaded")
    @NotEmpty
    private String name;

    @Schema(description = "Size of the audio file in bytes")
    @NotNull
    private Long length;
}
