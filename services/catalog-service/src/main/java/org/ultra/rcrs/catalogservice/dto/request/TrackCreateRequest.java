package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.ultra.rcrs.catalogservice.model.ArtistOther;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Validated
@Data
public class TrackCreateRequest {

    @NotNull
    private String title;

    @NotNull
    private Integer trackNumber;

    @NotNull
    private Boolean explicit;

    @NotEmpty
    private Set<ArtistId> artists = new HashSet<>();

    private List<ArtistOther> others = new ArrayList<>();
}