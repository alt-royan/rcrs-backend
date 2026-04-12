package org.ultra.rcrs.uploadservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ArtistCreateRequest {

    @NotNull
    private String name;

    @Pattern(regexp = "s3://[\\w\\-]+/[\\w\\-.]+", message = "URI must be s3://{bucket}/{key} formatted")
    private String avatarUri;
}
