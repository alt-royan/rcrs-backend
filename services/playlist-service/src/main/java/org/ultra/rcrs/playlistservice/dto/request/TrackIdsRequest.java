package org.ultra.rcrs.playlistservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TrackIdsRequest {

    @NotEmpty
    private List<String> trackIds;
}
