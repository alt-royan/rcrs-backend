package org.ultra.rcrs.mediaservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StreamingRequest {

    @NotNull
    private String trackId;
}
