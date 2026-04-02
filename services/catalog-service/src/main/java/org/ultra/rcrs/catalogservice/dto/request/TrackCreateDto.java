package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;

import java.util.Set;
import java.util.UUID;

@Validated
@Data
public class TrackCreateDto {

    @NotNull
    @NotBlank
    private String id;

    @NotNull
    private String title;

    @NotNull
    private UUID albumId;

    @NotEmpty
    private Set<ArtistWithRole> artists;

    @NotNull
    private Integer trackNumber;

    @NotNull
    private Boolean explicit;

}