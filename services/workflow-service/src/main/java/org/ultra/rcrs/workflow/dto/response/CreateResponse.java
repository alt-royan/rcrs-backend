package org.ultra.rcrs.workflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned once a creation workflow (artist registration, album upload) has completed.")
public record CreateResponse(
        @Schema(description = "Short (Base62) identifier assigned to the newly created entity.")
        String id
) {
}
