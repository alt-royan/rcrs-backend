package org.ultra.rcrs.metadata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialLinkDto {

    @NotBlank
    private String resourceName;

    @NotNull
    private URI url;
}
