package org.ultra.rcrs.searchservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.album.AlbumCreatedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.events.album.ArtistAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.album.ArtistDeletedFromAlbumEventOuterClass;
import org.ultra.rcrs.searchservice.document.*;
import org.ultra.rcrs.searchservice.repository.AlbumIndexRepository;
import org.ultra.rcrs.searchservice.repository.ArtistIndexRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumIndexService {

    private final AlbumIndexRepository albumIndexRepository;
    private final ArtistIndexRepository artistIndexRepository;

    public void handleAlbumCreated(AlbumCreatedEventOuterClass.AlbumCreatedEvent event) {
        EntityStatus availability = EntityStatus.valueOf(event.getAvailabilityStatus().name());
        LifecycleStatus lifecycle = LifecycleStatus.valueOf(event.getLifecycleStatus().name());

        var year = LocalDateTime.ofEpochSecond(event.getReleaseDate().getSeconds(), event.getReleaseDate().getNanos(), ZoneOffset.UTC).getYear();
        AlbumAdminDoc adminDoc = new AlbumAdminDoc();
        adminDoc.setId(event.getId());
        adminDoc.setTitle(event.getTitle());
        adminDoc.setAvailability(availability);
        adminDoc.setLifecycleStatus(lifecycle);
        adminDoc.setTracks(new ArrayList<>());
        adminDoc.setArtists(new ArrayList<>());
        adminDoc.setYear(String.valueOf(year));
        albumIndexRepository.index(adminDoc);

        log.info("Album created in admin index: id={}, availability={}, lifecycle={}", event.getId(), availability, lifecycle);
    }

    public void handleAlbumLifecycleStatusUpdated(AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent event) {
        AlbumAdminDoc adminDoc = albumIndexRepository.get(event.getId(), AlbumAdminDoc.class);
        if (adminDoc == null) return;

        LifecycleStatus newLifecycle = LifecycleStatus.valueOf(event.getLifecycleStatus().name());
        adminDoc.setLifecycleStatus(newLifecycle);
        albumIndexRepository.index(adminDoc);

        if (newLifecycle == LifecycleStatus.PUBLISHED) {
            publishAlbum(adminDoc);
        }

        log.info("Album lifecycle updated: id={}, lifecycle={}", event.getId(), newLifecycle);
    }

    public void handleArtistAddedToAlbum(ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent event) {
        AlbumAdminDoc adminDoc = albumIndexRepository.get(event.getAlbumId(), AlbumAdminDoc.class);
        if (adminDoc == null) return;

        ArtistAdminDoc artistDoc = artistIndexRepository.get(event.getArtistId(), ArtistAdminDoc.class);
        String artistName = artistDoc != null ? artistDoc.getName() : "";

        NestedArtist nested = new NestedArtist();
        nested.setId(event.getArtistId());
        nested.setName(artistName);

        if (adminDoc.getArtists() == null) {
            adminDoc.setArtists(new ArrayList<>());
        }
        adminDoc.getArtists().add(nested);
        albumIndexRepository.index(adminDoc);

        AlbumPublicDoc publicDoc = albumIndexRepository.get(event.getAlbumId(), AlbumPublicDoc.class);
        if (publicDoc != null) {
            if (publicDoc.getArtists() == null) {
                publicDoc.setArtists(new ArrayList<>());
            }
            publicDoc.getArtists().add(nested);
            albumIndexRepository.index(publicDoc);
        }

        if (artistDoc != null) {
            NestedAlbum nestedAlbum = new NestedAlbum();
            nestedAlbum.setId(adminDoc.getId());
            nestedAlbum.setTitle(adminDoc.getTitle());

            if (artistDoc.getAlbums() == null) {
                artistDoc.setAlbums(new ArrayList<>());
            }
            artistDoc.getAlbums().add(nestedAlbum);
            artistIndexRepository.index(artistDoc);

            ArtistPublicDoc artistPublicDoc = artistIndexRepository.get(event.getArtistId(), ArtistPublicDoc.class);
            if (artistPublicDoc != null) {
                if (artistPublicDoc.getAlbums() == null) {
                    artistPublicDoc.setAlbums(new ArrayList<>());
                }
                artistPublicDoc.getAlbums().add(nestedAlbum);
                artistIndexRepository.index(artistPublicDoc);
            }
        }

        log.info("Artist added to album: artistId={}, albumId={}", event.getArtistId(), event.getAlbumId());
    }

    public void handleArtistDeletedFromAlbum(ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent event) {
        AlbumAdminDoc adminDoc = albumIndexRepository.get(event.getAlbumId(), AlbumAdminDoc.class);
        if (adminDoc == null) return;

        if (adminDoc.getArtists() != null) {
            adminDoc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
        }
        albumIndexRepository.index(adminDoc);

        AlbumPublicDoc publicDoc = albumIndexRepository.get(event.getAlbumId(), AlbumPublicDoc.class);
        if (publicDoc != null) {
            if (publicDoc.getArtists() != null) {
                publicDoc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
            }
            albumIndexRepository.index(publicDoc);
        }

        ArtistAdminDoc artistDoc = artistIndexRepository.get(event.getArtistId(), ArtistAdminDoc.class);
        if (artistDoc != null) {
            if (artistDoc.getAlbums() != null) {
                artistDoc.getAlbums().removeIf(a -> a.getId().equals(event.getAlbumId()));
            }
            artistIndexRepository.index(artistDoc);

            ArtistPublicDoc artistPublicDoc = artistIndexRepository.get(event.getArtistId(), ArtistPublicDoc.class);
            if (artistPublicDoc != null) {
                if (artistPublicDoc.getAlbums() != null) {
                    artistPublicDoc.getAlbums().removeIf(a -> a.getId().equals(event.getAlbumId()));
                }
                artistIndexRepository.index(artistPublicDoc);
            }
        }

        log.info("Artist removed from album: artistId={}, albumId={}", event.getArtistId(), event.getAlbumId());
    }

    public void handleAlbumDeleted(String id) {
        AlbumAdminDoc adminDoc = albumIndexRepository.get(id, AlbumAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.DELETED);
            albumIndexRepository.index(adminDoc);
        }

        albumIndexRepository.delete(id, AlbumPublicDoc.class);
        log.info("Album deleted: id={}", id);
    }

    public void handleAlbumHidden(String id) {
        AlbumAdminDoc adminDoc = albumIndexRepository.get(id, AlbumAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.HIDDEN);
            albumIndexRepository.index(adminDoc);
        }

        AlbumPublicDoc publicDoc = albumIndexRepository.get(id, AlbumPublicDoc.class);
        if (publicDoc != null) {
            publicDoc.setAvailability(EntityStatus.HIDDEN);
            albumIndexRepository.index(publicDoc);
        }

        log.info("Album hidden: id={}", id);
    }

    public void handleAlbumActivated(String id) {
        AlbumAdminDoc adminDoc = albumIndexRepository.get(id, AlbumAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.ACTIVE);
            albumIndexRepository.index(adminDoc);

            AlbumPublicDoc publicDoc = albumIndexRepository.get(id, AlbumPublicDoc.class);
            if (publicDoc != null) {
                publicDoc.setAvailability(EntityStatus.ACTIVE);
                albumIndexRepository.index(publicDoc);
            } else if (adminDoc.getLifecycleStatus() == LifecycleStatus.PUBLISHED) {
                publishAlbum(adminDoc);
            }
        }

        log.info("Album activated: id={}", id);
    }

    private void publishAlbum(AlbumAdminDoc adminDoc) {
        AlbumPublicDoc publicDoc = toPublicDoc(adminDoc);
        albumIndexRepository.index(publicDoc);
        log.info("Album published to public index: id={}", adminDoc.getId());
    }

    public void handleAlbumTrueDeleted(String id) {
        albumIndexRepository.delete(id, AlbumAdminDoc.class);
        albumIndexRepository.delete(id, AlbumPublicDoc.class);
        log.info("Album permanently deleted: id={}", id);
    }

    private AlbumPublicDoc toPublicDoc(AlbumAdminDoc adminDoc) {
        AlbumPublicDoc publicDoc = new AlbumPublicDoc();
        publicDoc.setId(adminDoc.getId());
        publicDoc.setTitle(adminDoc.getTitle());
        publicDoc.setYear(adminDoc.getYear());
        publicDoc.setAvailability(adminDoc.getAvailability());
        publicDoc.setTracks(adminDoc.getTracks());
        publicDoc.setArtists(adminDoc.getArtists());
        return publicDoc;
    }
}
