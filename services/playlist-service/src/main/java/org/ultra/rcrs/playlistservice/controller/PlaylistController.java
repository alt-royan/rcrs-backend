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
import org.ultra.rcrs.playlistservice.service.PlaylistService;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final S3Utils s3Utils;

    @GetMapping
    public Flux<PlaylistViewDto> getPlaylists(@RequestParam List<String> ids) {
        return playlistService.getPlaylistsByIds(ids)
                .map(doc -> PlaylistMapper.toViewDto(doc, s3Utils));
    }

    @PostMapping
    public Mono<ResponseEntity<CreateResponse>> createPlaylist(@RequestBody @Validated CreatePlaylistRequest request,
                                                                @AuthenticationPrincipal Jwt jwt) {
        String id = Url62.encode(UUID.randomUUID());
        return playlistService.createPlaylist(id, jwt.getSubject(), request.getTitle(), request.getDescription(),
                        request.getTags(), request.getTrackIds(), s3Utils.parseKey(request.getCoverUri()), request.isPublic())
                .map(doc -> ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse(doc.getId())));
    }

    @GetMapping("/{playlistId}/tracks")
    public Flux<PlaylistTrackViewDto> getTracks(@PathVariable String playlistId,
                                                 @RequestParam(defaultValue = "0") int offset,
                                                 @RequestParam(defaultValue = "50") int limit,
                                                 @RequestParam(defaultValue = "position") String sortBy,
                                                 @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return playlistService.getTracks(playlistId, offset, limit, sortBy, direction)
                .map(PlaylistMapper::toTrackViewDto);
    }

    @PutMapping("/{playlistId}/tracks")
    public Mono<ResponseEntity<Void>> addTracks(@PathVariable String playlistId,
                                                 @RequestBody @Validated TrackIdsRequest request) {
        return playlistService.addTracks(playlistId, request.getTrackIds())
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    @DeleteMapping("/{playlistId}/tracks")
    public Mono<ResponseEntity<Void>> deleteTracks(@PathVariable String playlistId,
                                                    @RequestBody @Validated TrackIdsRequest request) {
        return playlistService.deleteTracks(playlistId, request.getTrackIds())
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    @DeleteMapping("/{playlistId}")
    public Mono<ResponseEntity<Void>> deletePlaylist(@PathVariable String playlistId) {
        return playlistService.deletePlaylist(playlistId)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
