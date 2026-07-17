package org.ultra.rcrs.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class OtherArtistDto {

    @NotBlank
    private String name;

    @NotEmpty
    private Set<ArtistRole> roles;

    private List<SocialLinkDto> socialLinks = new ArrayList<>();
}
