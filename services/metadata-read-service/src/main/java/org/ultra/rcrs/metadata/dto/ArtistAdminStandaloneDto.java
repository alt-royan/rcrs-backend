package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.EntityStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Artist summary, projected onto an album listing (admin view, unfiltered).")
public class ArtistAdminStandaloneDto {

    @Schema(description = "Base62-encoded short ID of the artist.")
    private String id;
    @Schema(description = "Artist display name.")
    private String name;
    @Schema(description = "URL of the artist's avatar image.")
    private String avatarUrl;
    @Schema(description = "Availability/visibility state of the artist (e.g. ACTIVE, HIDDEN, DELETED).")
    private EntityStatus availabilityStatus;
}
