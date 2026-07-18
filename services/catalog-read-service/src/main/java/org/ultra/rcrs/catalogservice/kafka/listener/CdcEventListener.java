package org.ultra.rcrs.catalogservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.AlbumDocument;
import org.ultra.rcrs.catalogservice.model.ArtistDocument;
import org.ultra.rcrs.catalogservice.model.TrackDocument;
import org.ultra.rcrs.catalogservice.repository.AlbumDocumentRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistDocumentRepository;
import org.ultra.rcrs.catalogservice.repository.TrackDocumentRepository;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.events.CatalogCdcEvent;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CdcEventListener {

    private final ObjectMapper objectMapper;
    private final ArtistDocumentRepository artistDocumentRepository;
    private final AlbumDocumentRepository albumDocumentRepository;
    private final TrackDocumentRepository trackDocumentRepository;

    @KafkaListener(topics = Topics.CATALOG_CDC_TOPIC, groupId = "read-group")
    public void handleCdcEvent(String message) {
        log.info("Received CDC event: {}", message);
        try {
            CatalogCdcEvent event = objectMapper.readValue(message, CatalogCdcEvent.class);

            switch (event.getEntityType()) {
                case CatalogCdcEvent.ARTIST -> handleArtistEvent(event);
                case CatalogCdcEvent.ALBUM -> handleAlbumEvent(event);
                case CatalogCdcEvent.TRACK -> handleTrackEvent(event);
                default -> log.warn("Unknown entity type: {}", event.getEntityType());
            }
        } catch (Exception e) {
            log.error("Failed to process CDC event: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleArtistEvent(CatalogCdcEvent event) {
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();
        String id = (String) payload.get("id");

        if (CatalogCdcEvent.DELETE.equals(event.getAction())) {
            artistDocumentRepository.deleteById(id).subscribe();
            log.info("Deleted artist document: {}", id);
        } else {
            ArtistDocument doc = ArtistDocument.builder()
                    .id(id)
                    .name((String) payload.get("name"))
                    .avatarUrl((String) payload.get("avatarUrl"))
                    .socialLinks(parseSocialLinks(payload.get("socialLinks")))
                    .build();
            artistDocumentRepository.save(doc).subscribe();
            log.info("Upserted artist document: {}", id);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleAlbumEvent(CatalogCdcEvent event) {
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();
        String id = (String) payload.get("id");

        if (CatalogCdcEvent.DELETE.equals(event.getAction())) {
            albumDocumentRepository.deleteById(id).subscribe();
            log.info("Deleted album document: {}", id);
        } else {
            List<AlbumDocument.ArtistEmbed> artists = parseAlbumArtists(payload.get("artists"));
            List<AlbumDocument.TrackEmbed> tracks = parseAlbumTracks(payload.get("tracks"));

            AlbumDocument doc = AlbumDocument.builder()
                    .id(id)
                    .status((String) payload.get("status"))
                    .title((String) payload.get("title"))
                    .type((String) payload.get("type"))
                    .releaseDate((String) payload.get("releaseDate"))
                    .year((Integer) payload.get("year"))
                    .totalTracks((Integer) payload.get("totalTracks"))
                    .totalDurationMs((Integer) payload.get("totalDurationMs"))
                    .coverUrl((String) payload.get("coverUrl"))
                    .explicit((Boolean) payload.get("explicit"))
                    .available((Boolean) payload.get("available"))
                    .artists(artists)
                    .tracks(tracks)
                    .build();
            albumDocumentRepository.save(doc).subscribe();
            log.info("Upserted album document: {}", id);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleTrackEvent(CatalogCdcEvent event) {
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();
        String id = (String) payload.get("id");

        if (CatalogCdcEvent.DELETE.equals(event.getAction())) {
            trackDocumentRepository.deleteById(id).subscribe();
            log.info("Deleted track document: {}", id);
        } else {
            Map<String, Object> albumMap = (Map<String, Object>) payload.get("album");
            TrackDocument.AlbumEmbed albumEmbed = albumMap != null ? TrackDocument.AlbumEmbed.builder()
                    .id((String) albumMap.get("id"))
                    .title((String) albumMap.get("title"))
                    .coverUrl((String) albumMap.get("coverUrl"))
                    .build() : null;

            List<TrackDocument.ArtistEmbed> artists = parseTrackArtists(payload.get("artists"));
            List<TrackDocument.OtherArtistEmbed> others = parseOtherArtists(payload.get("others"));

            TrackDocument doc = TrackDocument.builder()
                    .id(id)
                    .status((String) payload.get("status"))
                    .title((String) payload.get("title"))
                    .releaseDate((String) payload.get("releaseDate"))
                    .durationMs((Integer) payload.get("durationMs"))
                    .trackNumber((Integer) payload.get("trackNumber"))
                    .explicit((Boolean) payload.get("explicit"))
                    .available((Boolean) payload.get("available"))
                    .album(albumEmbed)
                    .artists(artists)
                    .others(others)
                    .build();
            trackDocumentRepository.save(doc).subscribe();
            log.info("Upserted track document: {}", id);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ArtistDocument.SocialLinkEmbed> parseSocialLinks(Object raw) {
        if (raw == null) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) raw;
        return list.stream().map(m -> new ArtistDocument.SocialLinkEmbed(
                (String) m.get("resourceName"), (String) m.get("url"))).toList();
    }

    @SuppressWarnings("unchecked")
    private List<AlbumDocument.ArtistEmbed> parseAlbumArtists(Object raw) {
        if (raw == null) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) raw;
        return list.stream().map(m -> new AlbumDocument.ArtistEmbed(
                (String) m.get("id"), (String) m.get("name"),
                (String) m.get("avatarUrl"), (String) m.get("role"))).toList();
    }

    @SuppressWarnings("unchecked")
    private List<AlbumDocument.TrackEmbed> parseAlbumTracks(Object raw) {
        if (raw == null) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) raw;
        return list.stream().map(m -> {
            var artists = parseTrackArtists(m.get("artists"));
            return new AlbumDocument.TrackEmbed(
                    (String) m.get("id"), (String) m.get("status"), (String) m.get("title"),
                    (Integer) m.get("durationMs"), (Integer) m.get("trackNumber"),
                    (Boolean) m.get("explicit"), (Boolean) m.get("available"), artists);
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<TrackDocument.ArtistEmbed> parseTrackArtists(Object raw) {
        if (raw == null) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) raw;
        return list.stream().map(m -> new TrackDocument.ArtistEmbed(
                (String) m.get("id"), (String) m.get("name"),
                (String) m.get("avatarUrl"), (String) m.get("role"))).toList();
    }

    @SuppressWarnings("unchecked")
    private List<TrackDocument.OtherArtistEmbed> parseOtherArtists(Object raw) {
        if (raw == null) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) raw;
        return list.stream().map(m -> {
            List<String> roles = m.get("roles") != null
                    ? ((List<Object>) m.get("roles")).stream().map(Object::toString).toList()
                    : List.of();
            var socialLinks = parseSocialLinks(m.get("socialLinks"));
            return new TrackDocument.OtherArtistEmbed((String) m.get("name"), roles, socialLinks);
        }).toList();
    }
}
