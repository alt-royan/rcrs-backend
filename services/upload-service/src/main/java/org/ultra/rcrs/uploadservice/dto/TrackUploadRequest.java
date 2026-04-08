package org.ultra.rcrs.uploadservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

@Validated
@Data
public class TrackUploadRequest {

    @NotEmpty
    private String uid;

    @NotNull
    private String title;

    @NotNull
    private Integer trackNumber;

    @NotNull
    private Boolean explicit;

    @NotEmpty
    private Set<ArtistId> artists;

    private List<ArtistOther> others;
}
