package org.ultra.rcrs.uploadservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PreloadFileRequest {

    @NotEmpty
    private String name;

    @NotNull
    private Long length;
}
