package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Schema(description = "A single external social media or website link belonging to an artist/contributor.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialLinkDto {

    @Schema(description = "Name of the linked resource/platform (e.g. \"Instagram\", \"Twitter\", \"Official Website\"). Required, must not be blank.", example = "Instagram")
    @NotBlank
    private String resourceName;

    @Schema(description = "Absolute URL of the profile/page on the given resource. Required.", example = "https://instagram.com/example")
    @NotNull
    private URI url;
}
