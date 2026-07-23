package org.ultra.rcrs.metadata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.EntityStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistAdminStandaloneDto {

    private String id;
    private String name;
    private String avatarUrl;
    private EntityStatus availabilityStatus;
}
