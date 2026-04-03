package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@Data
public class TrackCreateRequest {

    @NotNull
    private String title;

    @NotEmpty
    private Set<ArtistWithRole> artists;

    @NotNull
    private Integer trackNumber;

    @NotNull
    private Boolean explicit;

    @NotNull
    private Long durationMs;

}