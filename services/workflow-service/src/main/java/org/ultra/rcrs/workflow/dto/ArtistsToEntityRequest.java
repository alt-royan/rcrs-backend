package org.ultra.rcrs.workflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ArtistsToEntityRequest(@NotEmpty @Valid List<ArtistDto> artists) {
}
