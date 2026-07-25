package org.ultra.rcrs.playlistservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.ultra.rcrs.playlistservice.model.PlaylistType;

import java.util.List;

@Data
public class CreatePlaylistRequest {

    @NotBlank
    private String title;

    private String description;

    private List<String> tags;

    private List<String> trackIds;

    private String coverUri;

    private boolean isPrivate = true;

    private PlaylistType type = PlaylistType.CUSTOM;
}
