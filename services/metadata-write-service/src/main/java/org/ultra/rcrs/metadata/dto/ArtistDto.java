package org.ultra.rcrs.metadata.dto;

import lombok.Data;
import org.ultra.rcrs.enums.ArtistRole;

@Data
public class ArtistDto {

    private String id;

    private String name;

    private ArtistRole role = ArtistRole.MAIN_ARTIST;
}
