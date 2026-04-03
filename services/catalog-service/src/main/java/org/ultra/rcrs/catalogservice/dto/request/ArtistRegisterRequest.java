package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtistRegisterRequest {

    @NotNull
    private String name;

    private String bio;

    private String imageExternalKey;

}