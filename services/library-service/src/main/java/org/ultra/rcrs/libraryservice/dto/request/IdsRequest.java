package org.ultra.rcrs.libraryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "A batch of Base62-encoded short identifiers.")
public class IdsRequest {

    @NotEmpty
    @Size(max = 200, message = "must contain at most 200 ids")
    @Schema(description = "Base62-encoded short identifiers. At most 200 per request.")
    private List<String> ids;
}
