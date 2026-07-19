package org.ultra.rcrs.metadata.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.ultra.rcrs.metadata.dto.OtherArtistDto;

import java.util.List;

@Data
public class OthersToTrackRequest {

    @NotEmpty
    @Valid
    private List<OtherArtistDto> others;
}
