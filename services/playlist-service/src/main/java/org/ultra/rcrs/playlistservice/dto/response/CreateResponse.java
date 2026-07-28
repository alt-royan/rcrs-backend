package org.ultra.rcrs.playlistservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response returned after successfully creating a playlist.")
public class CreateResponse {

    @Schema(description = "Short (Base62) ID of the newly created playlist.", example = "1BvZ7t")
    private String id;
}
