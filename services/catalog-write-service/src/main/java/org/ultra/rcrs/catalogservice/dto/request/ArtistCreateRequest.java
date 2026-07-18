package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;

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
