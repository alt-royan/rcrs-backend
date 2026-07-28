package org.ultra.rcrs.libraryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.libraryservice.dto.ErrorResponse;
import org.ultra.rcrs.libraryservice.dto.request.IdsRequest;
import org.ultra.rcrs.libraryservice.dto.request.ListenReportRequest;
import org.ultra.rcrs.libraryservice.dto.response.*;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.service.ListenService;
import org.ultra.rcrs.security.CallerId;

import java.time.Instant;
import java.util.List;

@Tag(
        name = "Listen history",
        description = "The caller's playback history and everything derived from it. Playbacks are " +
                "reported by the client in batches — media-service only issues presigned URLs and never " +
                "sees the audio flow, so it cannot observe how much of a track was played. Batching is " +
                "also what lets a device report plays that happened while it was offline. " +
                "The 'recently played' shelf and play counts are served from maintained summaries rather " +
                "than by scanning the raw log."
)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/me/library")
public class ListenController {

    private final ListenService listenService;

    @Operation(
            summary = "Report a batch of playbacks",
            description = "Accepts playbacks buffered by the client. Supply clientPlayId on each entry to " +
                    "make the batch idempotent: a resent batch is recognised and not counted twice, and " +
                    "the response says how many entries were rejected for that reason."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "The batch was processed; the body reports how much of it counted"),
            @ApiResponse(responseCode = "400", description = "Empty batch, too many entries, or an entry failed validation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/listens")
    public ResponseEntity<ListenIngestResponse> report(
            @RequestBody @NotEmpty @Size(max = 200, message = "must contain at most 200 entries")
            List<@Valid ListenReportRequest> reports,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(listenService.ingest(CallerId.of(jwt), reports));
    }

    @Operation(
            summary = "Read the raw playback timeline",
            description = "Paginated by cursor rather than by offset, because the timeline is append-only " +
                    "and a deep offset would mean walking every newer entry. Pass the nextCursor from the " +
                    "previous response to continue; a null nextCursor means the end has been reached."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of the timeline, newest first"),
            @ApiResponse(responseCode = "400", description = "limit out of range, or an unparseable cursor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/listens")
    public ListenTimelineResponse timeline(
            @Parameter(description = "Return only playbacks strictly older than this ISO-8601 instant. " +
                    "Omit for the first page.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursor,
            @Parameter(description = "Maximum number of entries to return")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return listenService.timeline(CallerId.of(jwt), cursor, limit);
    }

    @Operation(
            summary = "Clear the playback timeline",
            description = "Removes the raw history. Play counts and the recently-played shelf are kept, " +
                    "since this clears what was listened to and when, not the caller's whole library."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The timeline is empty"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/listens")
    public ResponseEntity<Void> clear(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        listenService.clearTimeline(CallerId.of(jwt));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Read the 'recently played' shelf",
            description = "Distinct entities the caller played, most recent first. Each play is attributed " +
                    "to what it was started from — an album, playlist or artist — falling back to the " +
                    "track itself when the source was not a browsable entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The shelf entries"),
            @ApiResponse(responseCode = "400", description = "limit out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/recently-played")
    public List<RecentlyPlayedDto> recentlyPlayed(
            @Parameter(description = "Restrict to one kind of entity. Omit for all kinds.")
            @RequestParam(required = false) LibraryEntityType type,
            @Parameter(description = "Maximum number of entries to return")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return listenService.recentlyPlayed(CallerId.of(jwt), type, limit);
    }

    @Operation(
            summary = "Read the caller's most played tracks",
            description = "Ordered by number of recorded playbacks, all-time."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The caller's top tracks"),
            @ApiResponse(responseCode = "400", description = "limit out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/top-tracks")
    public List<TrackPlayCountDto> topTracks(
            @Parameter(description = "Maximum number of tracks to return")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return listenService.topTracks(CallerId.of(jwt), limit);
    }

    @Operation(
            summary = "Read play counts for a batch of tracks",
            description = "Returns one entry per requested track, in the order asked for. Tracks the " +
                    "caller has never played report a count of zero rather than being omitted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Play counts for the requested tracks"),
            @ApiResponse(responseCode = "400", description = "Empty batch, or more than 200 ids",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/play-counts")
    public List<TrackPlayCountDto> playCounts(
            @Valid @RequestBody IdsRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return listenService.playCounts(CallerId.of(jwt), request.getIds());
    }
}
