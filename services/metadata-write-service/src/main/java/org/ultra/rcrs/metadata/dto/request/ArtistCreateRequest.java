package org.ultra.rcrs.metadata.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.metadata.dto.SocialLinkDto;

import java.util.List;

@Schema(description = "Payload for creating a new artist.")
@Data
public class ArtistCreateRequest {

    @Schema(description = "Display name of the artist. Required.", example = "The Beatles")
    @NotNull
    private String name;

    @Schema(description = "URI/key of the artist avatar image previously uploaded to storage. Optional.")
    private String avatarUri;

    @Schema(description = "Social media/website links for the artist. Null is treated as an empty list.")
    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<SocialLinkDto> socialLinks;

    @Schema(description = "Free-form tags/genres associated with the artist. Null is treated as an empty list.")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> tags;
}
