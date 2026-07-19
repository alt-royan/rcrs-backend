package org.ultra.rcrs.workflow.dto;

import org.ultra.rcrs.enums.ArtistRole;

public record ArtistDto(String id, String name, ArtistRole role) {
}
