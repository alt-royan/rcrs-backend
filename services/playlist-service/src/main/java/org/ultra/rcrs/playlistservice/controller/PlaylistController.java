package org.ultra.rcrs.playlistservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.playlistservice.dto.request.CreatePlaylistRequest;
import org.ultra.rcrs.playlistservice.dto.request.TrackIdsRequest;
import org.ultra.rcrs.playlistservice.dto.response.CreateResponse;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistTrackViewDto;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistViewDto;
import org.ultra.rcrs.playlistservice.mapper.PlaylistMapper;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.service.PlaylistService;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final S3Utils s3Utils;

    @GetMapping
    public List<PlaylistViewDto> getPlaylists(@RequestParam List<String> ids) {
        return playlistService.getPlaylistsByIds(ids).stream()
                .map(playlist -> PlaylistMapper.toViewDto(playlist, s3Utils))
                .toList();
    }

    @PostMapping
    public ResponseEntity<CreateResponse> createPlaylist(@RequestBody @Validated CreatePlaylistRequest request,
                                                           @AuthenticationPrincipal Jwt jwt) {
        String id = Url62.encode(UUID.randomUUID());
        Playlist playlist = playlistService.createPlaylist(id, jwt.getSubject(), request.getTitle(), request.getDescription(),
                request.getTags(), request.getTrackIds(), s3Utils.parseKey(request.getCoverUri()), request.isPrivate(),
                request.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse(playlist.getId()));
    }

    @GetMapping("/{playlistId}/tracks")
    public List<PlaylistTrackViewDto> getTracks(@PathVariable String playlistId,
                                                 @RequestParam(defaultValue = "0") int offset,
                                                 @RequestParam(defaultValue = "50") int limit,
                                                 @RequestParam(defaultValue = "position") String sortBy,
                                                 @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return playlistService.getTracks(playlistId, offset, limit, sortBy, direction).stream()
                .map(PlaylistMapper::toTrackViewDto)
                .toList();
    }

    @PutMapping("/{playlistId}/tracks")
    public ResponseEntity<Void> addTracks(@PathVariable String playlistId,
                                           @RequestBody @Validated TrackIdsRequest request) {
        playlistService.addTracks(playlistId, request.getTrackIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/tracks")
    public ResponseEntity<Void> deleteTracks(@PathVariable String playlistId,
                                              @RequestBody @Validated TrackIdsRequest request) {
        playlistService.deleteTracks(playlistId, request.getTrackIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable String playlistId) {
        playlistService.deletePlaylist(playlistId);
        return ResponseEntity.noContent().build();
    }
}
