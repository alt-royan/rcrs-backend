package org.ultra.rcrs.mediaservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.ultra.rcrs.enums.FileStatus;

@Data
@AllArgsConstructor
@Schema(description = "Processing status of a previously uploaded file, keyed by the upload uid.")
public class FileStatusResponse {

    @Schema(description = "Upload uid this status refers to, as returned by the pre-sign endpoint")
    private String uid;

    @Schema(description = "Current lifecycle status of the file (e.g. pending, processing, completed, failed)")
    private FileStatus status;

    @Schema(description = "Human-readable failure reason, present only when status indicates a failure")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;
}
