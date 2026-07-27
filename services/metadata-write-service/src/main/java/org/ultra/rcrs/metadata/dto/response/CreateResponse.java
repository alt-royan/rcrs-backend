package org.ultra.rcrs.metadata.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

@Schema(description = "Response returned after successfully creating a catalog entity (artist, album, or track).")
@Data
public class CreateResponse {

    @Schema(description = "Base62-encoded (Url62) public identifier of the newly created entity.", example = "1a2B3c4D5e")
    private String id;

    public CreateResponse(UUID uuid) {
        this.id = Url62.encode(uuid);
    }
}
