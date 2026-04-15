package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.ultra.rcrs.enums.ArtistRole;

@Data
public class ArtistIdDto {

    @NotNull
    private String id;

    private ArtistRole role = ArtistRole.MAIN_ARTIST;
}
