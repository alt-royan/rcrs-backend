package org.ultra.rcrs.playlistservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.model.PlaylistType;
import org.ultra.rcrs.playlistservice.repository.OffsetBasedPageRequest;
import org.ultra.rcrs.playlistservice.repository.PlaylistRepository;
import org.ultra.rcrs.playlistservice.repository.PlaylistTrackRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    @Transactional
    public Playlist createPlaylist(String id, String ownerId, String title, String description,
                                    List<String> tags, List<String> trackIds, String coverS3Key,
                                    boolean isPrivate, PlaylistType type) {
        var now = Instant.now();
        List<String> distinctTrackIds = trackIds != null ? trackIds.stream().distinct().toList() : List.of();

        Playlist playlist = Playlist.builder()
                .id(id)
                .ownerId(ownerId)
                .title(title)
                .description(description)
                .tags(tags != null ? tags : List.of())
                .coverS3Key(coverS3Key)
                .isPrivate(isPrivate)
                .type(type != null ? type : PlaylistType.CUSTOM)
                .trackCount(distinctTrackIds.size())
                .createdAt(now)
                .updatedAt(now)
                .build();

        for (int i = 0; i < distinctTrackIds.size(); i++) {
            playlist.getTracks().add(PlaylistTrack.builder()
                    .playlist(playlist)
                    .trackId(distinctTrackIds.get(i))
                    .position(i)
                    .addedAt(now)
                    .build());
        }

        Playlist saved = playlistRepository.save(playlist);
        log.info("Created playlist: id={}, ownerId={}", saved.getId(), saved.getOwnerId());
        return saved;
    }

    public List<Playlist> getPlaylistsByIds(List<String> ids) {
        return playlistRepository.findAllByIdIn(ids);
    }

    public List<PlaylistTrack> getTracks(String playlistId, int offset, int limit, String sortBy, Sort.Direction direction) {
        String sortField = "addedAt".equalsIgnoreCase(sortBy) ? "addedAt" : "position";
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by(direction, sortField));
        return playlistTrackRepository.findByPlaylistId(playlistId, pageable);
    }

    @Transactional
    public Playlist addTracks(String playlistId, List<String> trackIds) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist", playlistId));

        List<PlaylistTrack> existing = playlist.getTracks();
        Set<String> existingIds = existing.stream().map(PlaylistTrack::getTrackId).collect(Collectors.toSet());
        List<String> toAdd = trackIds.stream().distinct().filter(trackId -> !existingIds.contains(trackId)).toList();
        if (toAdd.isEmpty()) {
            return playlist;
        }

        int startPosition = existing.size();
        var now = Instant.now();
        for (int i = 0; i < toAdd.size(); i++) {
            existing.add(PlaylistTrack.builder()
                    .playlist(playlist)
                    .trackId(toAdd.get(i))
                    .position(startPosition + i)
                    .addedAt(now)
                    .build());
        }
        playlist.setTrackCount(existing.size());
        playlist.setUpdatedAt(now);

        Playlist saved = playlistRepository.save(playlist);
        log.info("Added {} tracks to playlist: id={}", trackIds.size(), playlistId);
        return saved;
    }

    @Transactional
    public Playlist deleteTracks(String playlistId, List<String> trackIds) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist", playlistId));

        List<PlaylistTrack> tracks = playlist.getTracks();
        boolean removed = tracks.removeIf(t -> trackIds.contains(t.getTrackId()));
        if (!removed) {
            return playlist;
        }

        tracks.sort(Comparator.comparingInt(PlaylistTrack::getPosition));
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setPosition(i);
        }
        playlist.setTrackCount(tracks.size());
        playlist.setUpdatedAt(Instant.now());

        Playlist saved = playlistRepository.save(playlist);
        log.info("Removed tracks from playlist: id={}, trackIds={}", playlistId, trackIds);
        return saved;
    }

    @Transactional
    public void deletePlaylist(String playlistId) {
        playlistRepository.deleteById(playlistId);
        log.info("Deleted playlist: id={}", playlistId);
    }
}
