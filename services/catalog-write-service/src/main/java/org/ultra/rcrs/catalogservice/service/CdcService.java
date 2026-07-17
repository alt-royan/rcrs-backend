package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.model.write.*;
import org.ultra.rcrs.catalogservice.repository.write.*;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.kafka.events.CatalogCdcEvent;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CdcService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final S3Utils s3Utils;

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final ArtistToTrackRepository artistToTrackRepository;
    private final OtherArtistRepository otherArtistRepository;

    public void artistCreated(UUID artistId) {
        var artist = artistRepository.findById(artistId).orElseThrow();
        var doc = buildArtistDoc(artist);
        send(CatalogCdcEvent.UPSERT, CatalogCdcEvent.ARTIST, doc);
    }

    public void albumCreated(UUID albumId) {
        var doc = buildAlbumDoc(albumId);
        send(CatalogCdcEvent.UPSERT, CatalogCdcEvent.ALBUM, doc);
    }

    public void albumUpserted(UUID albumId) {
        var doc = buildAlbumDoc(albumId);
        send(CatalogCdcEvent.UPSERT, CatalogCdcEvent.ALBUM, doc);
    }

    public void albumDeleted(UUID albumId) {
        send(CatalogCdcEvent.DELETE, CatalogCdcEvent.ALBUM, Map.of("id", Url62.encode(albumId)));
    }

    public void trackCreated(UUID trackId, String uid) {
        var doc = buildTrackDoc(trackId);
        send(CatalogCdcEvent.UPSERT, CatalogCdcEvent.TRACK, doc);
    }

    public void trackUpserted(UUID trackId) {
        var doc = buildTrackDoc(trackId);
        send(CatalogCdcEvent.UPSERT, CatalogCdcEvent.TRACK, doc);
    }

    public void trackDeleted(UUID trackId) {
        send(CatalogCdcEvent.DELETE, CatalogCdcEvent.TRACK, Map.of("id", Url62.encode(trackId)));
    }

    private Map<String, Object> buildArtistDoc(Artist artist) {
        return Map.of(
                "id", Url62.encode(artist.getId()),
                "name", artist.getName(),
                "avatarUrl", s3Utils.parseUrl(artist.getAvatarS3Key()) != null ? s3Utils.parseUrl(artist.getAvatarS3Key()) : "",
                "socialLinks", artist.getSocialLinks() != null && artist.getSocialLinks().getItems() != null
                        ? artist.getSocialLinks().getItems().stream()
                        .map(l -> Map.of("resourceName", l.getResourceName(), "url", l.getUrl()))
                        .toList()
                        : List.of()
        );
    }

    private Map<String, Object> buildAlbumDoc(UUID albumId) {
        var album = albumRepository.findById(albumId).orElseThrow();
        var artistToAlbums = artistToAlbumRepository.findByAlbumId(albumId);
        var tracks = trackRepository.findAllByAlbumId(albumId);

        var artists = artistToAlbums.stream().map(ata -> {
            var a = artistRepository.findById(ata.getArtistId()).orElse(null);
            if (a == null) return Map.of();
            return Map.of(
                    "id", Url62.encode(a.getId()),
                    "name", a.getName(),
                    "avatarUrl", s3Utils.parseUrl(a.getAvatarS3Key()) != null ? s3Utils.parseUrl(a.getAvatarS3Key()) : "",
                    "role", ata.getArtistRole().name()
            );
        }).toList();

        var trackDocs = tracks.stream().map(t -> {
            var trackArtists = artistToTrackRepository.findByTrackId(t.getId()).stream().map(att -> {
                var a = artistRepository.findById(att.getArtistId()).orElse(null);
                if (a == null) return Map.of();
                return Map.of(
                        "id", Url62.encode(a.getId()),
                        "name", a.getName(),
                        "avatarUrl", s3Utils.parseUrl(a.getAvatarS3Key()) != null ? s3Utils.parseUrl(a.getAvatarS3Key()) : "",
                        "role", att.getArtistRole().name()
                );
            }).toList();
            return Map.<String, Object>of(
                    "id", Url62.encode(t.getId()),
                    "status", t.getStatus().name(),
                    "title", t.getTitle(),
                    "durationMs", t.getDurationMs() != null ? t.getDurationMs() : 0,
                    "trackNumber", t.getTrackNumber(),
                    "explicit", t.getExplicit(),
                    "available", t.getAvailable(),
                    "artists", trackArtists
            );
        }).toList();

        int totalTracks = tracks.size();
        int totalDurationMs = tracks.stream().mapToInt(t -> t.getDurationMs() != null ? t.getDurationMs() : 0).sum();
        boolean explicit = tracks.stream().allMatch(t -> Boolean.TRUE.equals(t.getExplicit()));

        return Map.of(
                "id", Url62.encode(album.getId()),
                "status", album.getStatus().name(),
                "title", album.getTitle(),
                "type", album.getType().getValue(),
                "releaseDate", album.getReleaseDate() != null ? album.getReleaseDate().toString() : "",
                "year", album.getReleaseDate() != null ? album.getReleaseDate().atZone(java.time.ZoneId.systemDefault()).getYear() : 0,
                "totalTracks", totalTracks,
                "totalDurationMs", totalDurationMs,
                "coverUrl", s3Utils.parseUrl(album.getCoverS3Key()) != null ? s3Utils.parseUrl(album.getCoverS3Key()) : "",
                "explicit", explicit,
                "available", album.getAvailable(),
                "artists", artists,
                "tracks", trackDocs
        );
    }

    private Map<String, Object> buildTrackDoc(UUID trackId) {
        var track = trackRepository.findById(trackId).orElseThrow();
        var album = albumRepository.findById(track.getAlbumId()).orElse(null);
        var trackArtists = artistToTrackRepository.findByTrackId(trackId).stream().map(att -> {
            var a = artistRepository.findById(att.getArtistId()).orElse(null);
            if (a == null) return Map.of();
            return Map.of(
                    "id", Url62.encode(a.getId()),
                    "name", a.getName(),
                    "avatarUrl", s3Utils.parseUrl(a.getAvatarS3Key()) != null ? s3Utils.parseUrl(a.getAvatarS3Key()) : "",
                    "role", att.getArtistRole().name()
            );
        }).toList();

        var others = otherArtistRepository.findByTrackId(trackId).stream().map(o -> {
            var socialLinks = o.getSocialLinks() != null && o.getSocialLinks().getItems() != null
                    ? o.getSocialLinks().getItems().stream()
                    .map(l -> Map.of("resourceName", l.getResourceName(), "url", l.getUrl()))
                    .toList()
                    : List.of();
            return Map.<String, Object>of(
                    "name", o.getName() != null ? o.getName() : "",
                    "roles", o.getRoles() != null ? o.getRoles().stream().map(Enum::name).toList() : List.of(),
                    "socialLinks", socialLinks
            );
        }).toList();

        var albumDoc = album != null ? Map.<String, Object>of(
                "id", Url62.encode(album.getId()),
                "title", album.getTitle(),
                "coverUrl", s3Utils.parseUrl(album.getCoverS3Key()) != null ? s3Utils.parseUrl(album.getCoverS3Key()) : ""
        ) : Map.of();

        return Map.of(
                "id", Url62.encode(track.getId()),
                "status", track.getStatus().name(),
                "title", track.getTitle(),
                "releaseDate", track.getReleaseDate() != null ? track.getReleaseDate().toString() : "",
                "durationMs", track.getDurationMs() != null ? track.getDurationMs() : 0,
                "trackNumber", track.getTrackNumber(),
                "explicit", track.getExplicit(),
                "available", track.getAvailable(),
                "album", albumDoc,
                "artists", trackArtists,
                "others", others
        );
    }

    private void send(String action, String entityType, Object payload) {
        try {
            var event = new CatalogCdcEvent(action, entityType, payload);
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(Topics.CATALOG_CDC_TOPIC, UUID.randomUUID().toString(), json);
            log.info("CDC event sent: action={} entityType={}", action, entityType);
        } catch (Exception e) {
            log.error("Failed to send CDC event: {}", e.getMessage(), e);
        }
    }
}
