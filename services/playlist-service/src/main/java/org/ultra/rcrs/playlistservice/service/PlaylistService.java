package org.ultra.rcrs.playlistservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.playlistservice.dto.response.PaginationResponse;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistStandaloneDto;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistTrackViewDto;
import org.ultra.rcrs.playlistservice.dto.response.PlaylistViewDto;
import org.ultra.rcrs.playlistservice.mapper.PlaylistMapper;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.model.PlaylistTrackPK;
import org.ultra.rcrs.playlistservice.model.PlaylistType;
import org.ultra.rcrs.playlistservice.repository.OffsetBasedPageRequest;
import org.ultra.rcrs.playlistservice.repository.PlaylistRepository;
import org.ultra.rcrs.playlistservice.repository.PlaylistTrackRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final PlaylistMapper mapper;

    @Transactional
    public UUID createPlaylist(UUID id, String ownerId, String title, String description,
                               List<String> tags, List<String> trackIds, String coverS3Key,
                               boolean isPrivate, PlaylistType type) {
        var now = Instant.now();

        Playlist playlist = Playlist.builder()
                .id(id)
                .ownerId(ownerId)
                .title(title)
                .description(description)
                .tags(tags != null ? tags : List.of())
                .coverS3Key(coverS3Key)
                .isPrivate(isPrivate)
                .type(type != null ? type : PlaylistType.CUSTOM)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<String> distinctTrackIds = trackIds != null ? trackIds.stream().distinct().toList() : List.of();

        List<PlaylistTrack> tracks = new ArrayList<>();

        for (int i = 0; i < distinctTrackIds.size(); i++) {
            tracks.add(PlaylistTrack.builder()
                    .playlistId(id)
                    .trackId(distinctTrackIds.get(i))
                    .position(i + 1)
                    .addedAt(now)
                    .build());
        }

        var saved = playlistRepository.save(playlist);
        playlistTrackRepository.saveAll(tracks);
        log.info("Created playlist: id={}, ownerId={}", saved.getId(), saved.getOwnerId());
        return saved.getId();
    }

    public PlaylistViewDto getPlaylistById(UUID id, String requesterId) {
        var playlist = playlistRepository.findByIdWithTrackCount(id)
                .orElseThrow(() -> new NotFoundException("Playlist", id));
        if (isHiddenFrom(playlist.isPrivate(), playlist.ownerId(), requesterId)) {
            throw new AccessDeniedException("It is private playlist");
        }
        return mapper.toViewDto(playlist);
    }

    /**
     * Someone else's private playlists are silently dropped from the result rather
     * than failing the whole batch, so a stale id in the caller's list degrades to
     * a missing entry instead of an error.
     */
    public List<PlaylistStandaloneDto> getPlaylistsByIds(List<UUID> ids, String requesterId) {
        return playlistRepository.findAllByIdIn(ids).stream()
                .filter(playlist -> !isHiddenFrom(playlist.getIsPrivate(), playlist.getOwnerId(), requesterId))
                .map(mapper::toStandaloneDto)
                .toList();
    }

    /** The caller's own playlists, most recently updated first. */
    public PaginationResponse<PlaylistStandaloneDto> getOwnPlaylists(String ownerId, int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.unsorted());
        List<PlaylistStandaloneDto> playlists = playlistRepository
                .findByOwnerIdOrderByUpdatedAtDesc(ownerId, pageable).stream()
                .map(mapper::toStandaloneDto)
                .toList();
        return new PaginationResponse<>(playlists, playlistRepository.countByOwnerId(ownerId), offset, limit);
    }

    public PaginationResponse<PlaylistTrackViewDto> getTracks(UUID playlistId, String requesterId, int offset, int limit, String sortBy, Sort.Direction direction) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist", playlistId));
        if (isHiddenFrom(playlist.getIsPrivate(), playlist.getOwnerId(), requesterId)) {
            throw new AccessDeniedException("It is private playlist");
        }

        String sortField = "addedAt".equalsIgnoreCase(sortBy) ? "addedAt" : "position";
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by(direction, sortField));
        List<PlaylistTrackViewDto> tracks = playlistTrackRepository.findByPlaylistId(playlistId, pageable).stream()
                .map(mapper::toTrackViewDto).toList();
        Integer count = playlistTrackRepository.countByPlaylistId(playlistId);
        return new PaginationResponse<>(tracks, count, offset, limit);
    }

    /**
     * A private playlist belonging to someone else is treated as absent rather than
     * forbidden: answering 403 would confirm that the id exists, which is exactly
     * what a private playlist should not reveal.
     */
    private static boolean isHiddenFrom(Boolean isPrivate, String ownerId, String requesterId) {
        return Boolean.TRUE.equals(isPrivate) && !Objects.equals(ownerId, requesterId);
    }

    @Transactional
    public void addTracks(UUID playlistId, List<String> trackIds, String ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist", playlistId));
        if (!playlist.getOwnerId().equals(ownerId)){
            throw new AccessDeniedException("You are not allowed to add tracks at this playlist. You are not it's owner.");
        }
        Set<String> existingIds = playlistTrackRepository.findAllByPlaylistId(playlist.getId()).stream()
                .map(PlaylistTrack::getTrackId)
                .collect(Collectors.toSet());
        List<String> toAdd = trackIds.stream().distinct().filter(trackId -> !existingIds.contains(trackId)).toList();
        if (toAdd.isEmpty()) {
            return;
        }

        var now = Instant.now();
        var lastPosition = existingIds.size();
        for (int i = 0; i < toAdd.size(); i++) {
            playlistTrackRepository.save(PlaylistTrack.builder()
                    .playlistId(playlist.getId())
                    .trackId(toAdd.get(i))
                    .position(lastPosition + i + 1)
                    .addedAt(now)
                    .build());
        }

        playlist.setUpdatedAt(now);

        playlistRepository.save(playlist);
        log.info("Added {} tracks to playlist: id={}", trackIds.size(), playlistId);
    }

    @Transactional
    public void deleteTracks(UUID playlistId, List<String> trackIds, String ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist", playlistId));
        if (!playlist.getOwnerId().equals(ownerId)){
            throw new AccessDeniedException("You are not allowed to delete tracks from this playlist. You are not it's owner.");
        }
        List<PlaylistTrackPK> forDelete = trackIds.stream().distinct()
                .map(id -> new PlaylistTrackPK(playlist.getId(), id))
                .toList();
        playlistTrackRepository.deleteAllById(forDelete);
        List<PlaylistTrack> survivors = playlistTrackRepository.findAllByPlaylistId(playlist.getId());

        survivors.sort(Comparator.comparingInt(PlaylistTrack::getPosition));
        for (int i = 0; i < survivors.size(); i++) {
            survivors.get(i).setPosition(i + 1);
        }
        playlist.setUpdatedAt(Instant.now());

        playlistRepository.save(playlist);
        playlistTrackRepository.saveAll(survivors);
        log.info("Removed tracks from playlist: id={}, trackIds={}", playlistId, trackIds);
    }

    @Transactional
    public void deletePlaylist(UUID playlistId, String ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist", playlistId));
        if (!playlist.getOwnerId().equals(ownerId)){
            throw new AccessDeniedException("You are not allowed to delete this playlist. You are not it's owner.");
        }
        playlistRepository.deleteById(playlistId);
        log.info("Deleted playlist: id={}", playlistId);
    }
}
