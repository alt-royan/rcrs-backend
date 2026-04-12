package org.ultra.rcrs.uploadservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageUploadRequest {

    @NotNull
    private String image;
}
