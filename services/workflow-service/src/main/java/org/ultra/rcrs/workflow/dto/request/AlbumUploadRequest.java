package org.ultra.rcrs.workflow.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotNull;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.workflow.dto.ArtistDto;

import java.time.OffsetDateTime;
import java.util.List;

public record AlbumUploadRequest(@NotNull String title, @NotNull AlbumType type, OffsetDateTime releaseDate,
                                 OffsetDateTime publishTimestamp, String coverUri,
                                 @JsonSetter(nulls = Nulls.AS_EMPTY) List<ArtistDto> artists,
                                 @JsonSetter(nulls = Nulls.AS_EMPTY) List<TrackUploadRequest> tracks) {
}