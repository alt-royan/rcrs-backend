package org.ultra.rcrs.metadata.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.metadata.dto.SocialLinkDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class ArtistCreateRequest {

    @NotNull
    private String name;

    private String avatarUri;

    @Valid
    private List<SocialLinkDto> socialLinks = new ArrayList<>();

    private List<String> tags = new ArrayList<>();
}
