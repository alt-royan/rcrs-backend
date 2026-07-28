package org.ultra.rcrs.libraryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.libraryservice.model.PlaybackSource;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "One playback the client is reporting. Clients buffer these and flush them in " +
        "batches, which is also what makes offline playback reportable once the device reconnects.")
public class ListenReportRequest {

    @NotBlank
    @Schema(description = "Base62-encoded short identifier of the track that was played.")
    private String trackId;

    @Schema(description = "Base62-encoded short identifier of the track's album, if known. " +
            "Recorded so album-level history needs no lookup.")
    private String albumId;

    @NotNull
    @Schema(description = "When playback started, as an ISO-8601 UTC instant. Supplied by the client " +
            "so that plays buffered while offline keep their real time.")
    private Instant playedAt;

    @PositiveOrZero
    @Schema(description = "How many milliseconds of the track were actually played.")
    private int msPlayed;

    @Schema(description = "Total length of the track in milliseconds. Used to decide whether the play " +
            "counts as completed; omit it and the play is never marked complete.")
    private Integer trackMs;

    @Schema(description = "What the user was playing from — an album, a playlist, an artist page. " +
            "Optional, but it cannot be reconstructed later and it is what drives the " +
            "'recently played' shelf.")
    private PlaybackSource sourceType;

    @Schema(description = "Base62-encoded short identifier of the album, playlist or artist named by " +
            "sourceType.")
    private String sourceId;

    @Schema(description = "Client-generated identifier for this playback, unique per user. Supply it to " +
            "make retried batches idempotent; without it a resent batch is counted twice.")
    private String clientPlayId;
}
