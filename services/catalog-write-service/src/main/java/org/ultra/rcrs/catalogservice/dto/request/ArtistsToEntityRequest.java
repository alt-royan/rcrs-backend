package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;

import java.util.List;

@Data
public class ArtistsToEntityRequest {

    @NotEmpty
    @Valid
    private List<ArtistDto> artists;
}
