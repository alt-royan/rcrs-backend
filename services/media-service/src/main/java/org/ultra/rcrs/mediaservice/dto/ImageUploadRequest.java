package org.ultra.rcrs.mediaservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageUploadRequest {

    @NotNull
    private String image;
}
