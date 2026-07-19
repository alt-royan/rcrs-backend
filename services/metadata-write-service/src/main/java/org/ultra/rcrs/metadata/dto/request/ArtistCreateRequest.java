package org.ultra.rcrs.metadata.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
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
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<SocialLinkDto> socialLinks;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> tags;
}
