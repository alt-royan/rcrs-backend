package org.ultra.rcrs.libraryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.Quality;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Records that the caller downloaded an album or playlist. " +
        "The client supplies the track ids because library-service holds no catalog data " +
        "and deliberately makes no calls to other services.")
public class DownloadCollectionRequest {

    @NotNull
    @Schema(description = "Kind of collection. Only ALBUM and PLAYLIST are accepted.", example = "ALBUM")
    private LibraryEntityType entityType;

    @NotBlank
    @Schema(description = "Base62-encoded short identifier of the album or playlist.")
    private String entityId;

    @NotNull
    @Schema(description = "Audio quality the files were downloaded at.", example = "HIGH")
    private Quality quality;

    @NotEmpty
    @Size(max = 1000, message = "must contain at most 1000 track ids")
    @Schema(description = "Base62-encoded short identifiers of the tracks that were downloaded.")
    private List<String> trackIds;
}
