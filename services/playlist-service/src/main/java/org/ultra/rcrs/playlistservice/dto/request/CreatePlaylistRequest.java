package org.ultra.rcrs.playlistservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlaylistRequest {

    @NotBlank
    private String title;

    private String description;

    private String coverUri;

    private boolean isPublic;
}
