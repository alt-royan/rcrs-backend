package org.ultra.rcrs.searchservice.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The kind of catalog entity to search for. Multiple values may be supplied in a single request to search across several kinds at once.")
public enum SearchType {
    @Schema(description = "Match artist documents.")
    artist,
    @Schema(description = "Match album documents.")
    album,
    @Schema(description = "Match track documents.")
    track
}