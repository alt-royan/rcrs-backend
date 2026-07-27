package org.ultra.rcrs.playlistservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.playlistservice.dto.ErrorResponse;
import org.ultra.rcrs.playlistservice.dto.request.CreatePlaylistRequest;
import org.ultra.rcrs.playlistservice.dto.request.IdsRequest;
import org.ultra.rcrs.playlistservice.dto.response.*;
import org.ultra.rcrs.playlistservice.service.PlaylistService;
import org.ultra.rcrs.utils.ImageUtils;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlists")
@Tag(name = "Playlist", description = "Synchronous REST API for managing user playlists and their tracks. " +
        "This is one of two ways playlist commands can be applied — the other being an asynchronous Kafka " +
        "command consumer that is not part of this controller.")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final ImageUtils imageUtils;

    @PostMapping("/get")
    @Operation(summary = "Get playlists by ID",
            description = "Fetches one or more playlists by their short (Base62) IDs and returns their public view, " +
                    "including a resolved cover image URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlists retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or malformed 'ids' parameter, or an ID is not a valid short ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<PlaylistStandaloneDto> getPlaylists(@RequestBody IdsRequest request) {
        List<UUID> uuids = request.getIds().stream().map(Url62::decode).toList();
        return playlistService.getPlaylistsByIds(uuids);
    }

    @GetMapping("/{playlistId}")
    @Operation(summary = "Get playlist by ID",
            description = "Fetches one playlist by their short (Base62) IDs and returns their public view, " +
                    "including a resolved cover image URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlists retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or malformed 'ids' parameter, or an ID is not a valid short ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PlaylistViewDto getPlaylists(@PathVariable("playlistId") String playlistId) {
        return playlistService.getPlaylistById(Url62.decode(playlistId));
    }

    @PostMapping
    @Operation(summary = "Create a playlist",
            description = "Creates a new playlist owned by the currently authenticated user. The owner ID is not " +
                    "supplied in the request body — it is derived from the caller's JWT subject.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Playlist created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation (e.g. blank title) or is malformed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Playlist conflicts with the current state of the resource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CreateResponse> createPlaylist(
            @Parameter(description = "Details of the playlist to create.", required = true)
            @RequestBody @Validated CreatePlaylistRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        UUID id = UUID.randomUUID();
        var playlistId = playlistService.createPlaylist(id, jwt.getSubject(), request.getTitle(), request.getDescription(),
                request.getTags(), request.getTrackIds(), imageUtils.parseKey(request.getCoverUri()), request.isPrivate(), request.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse(Url62.encode(playlistId)));
    }

    @GetMapping("/{playlistId}/tracks")
    @Operation(summary = "List tracks in a playlist",
            description = "Returns a paginated, sortable list of the tracks belonging to the given playlist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PaginationResponse<PlaylistTrackViewDto> getTracks(
            @Parameter(description = "Short (Base62) ID of the playlist.", required = true)
            @PathVariable String playlistId,
            @Parameter(description = "Zero-based offset of the first track to return.")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Maximum number of tracks to return.")
            @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Field to sort the tracks by.")
            @RequestParam(defaultValue = "position") String sortBy,
            @Parameter(description = "Sort direction.")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return playlistService.getTracks(Url62.decode(playlistId), offset, limit, sortBy, direction);
    }

    @PutMapping("/{playlistId}/tracks")
    @Operation(summary = "Add tracks to a playlist",
            description = "Appends the given track IDs to the specified playlist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracks added successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation (e.g. empty track list) or is malformed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not permitted to modify this playlist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> addTracks(
            @Parameter(description = "Short (Base62) ID of the playlist to add tracks to.", required = true)
            @PathVariable String playlistId,
            @Parameter(description = "Track IDs to add to the playlist.", required = true)
            @RequestBody @Validated IdsRequest request) {
        playlistService.addTracks(Url62.decode(playlistId), request.getIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/tracks")
    @Operation(summary = "Remove tracks from a playlist",
            description = "Removes the given track IDs from the specified playlist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracks removed successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation (e.g. empty track list) or is malformed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not permitted to modify this playlist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTracks(
            @Parameter(description = "Short (Base62) ID of the playlist to remove tracks from.", required = true)
            @PathVariable String playlistId,
            @Parameter(description = "Track IDs to remove from the playlist.", required = true)
            @RequestBody @Validated IdsRequest request) {
        playlistService.deleteTracks(Url62.decode(playlistId), request.getIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}")
    @Operation(summary = "Delete a playlist",
            description = "Permanently deletes the specified playlist.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Playlist deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not permitted to delete this playlist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletePlaylist(
            @Parameter(description = "Short (Base62) ID of the playlist to delete.", required = true)
            @PathVariable String playlistId) {
        playlistService.deletePlaylist(Url62.decode(playlistId));
        return ResponseEntity.noContent().build();
    }
}
