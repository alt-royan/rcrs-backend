package org.ultra.rcrs.mediaservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PreloadFileRequest {

    @NotEmpty
    private String fileName;

    @NotNull
    private Long fileSize;
}
