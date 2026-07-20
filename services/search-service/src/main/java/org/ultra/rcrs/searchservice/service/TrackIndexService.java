package org.ultra.rcrs.searchservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.track.*;
import org.ultra.rcrs.searchservice.document.*;
import org.ultra.rcrs.searchservice.repository.AlbumIndexRepository;
import org.ultra.rcrs.searchservice.repository.ArtistIndexRepository;
import org.ultra.rcrs.searchservice.repository.TrackIndexRepository;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackIndexService {

    private final TrackIndexRepository trackIndexRepository;
    private final ArtistIndexRepository artistIndexRepository;
    private final AlbumIndexRepository albumIndexRepository;

    public void handleTrackCreated(TrackCreatedEventOuterClass.TrackCreatedEvent event) {
        EntityStatus availability = EntityStatus.valueOf(event.getAvailabilityStatus().name());
        LifecycleStatus lifecycle = LifecycleStatus.valueOf(event.getLifecycleStatus().name());

        TrackAdminDoc adminDoc = new TrackAdminDoc();
        adminDoc.setId(event.getId());
        adminDoc.setTitle(event.getTitle());
        adminDoc.setAvailability(availability);
        adminDoc.setLifecycleStatus(lifecycle);
        adminDoc.setArtists(new ArrayList<>());
        trackIndexRepository.index(adminDoc);

        log.info("Track created in admin index: id={}, availability={}, lifecycle={}", event.getId(), availability, lifecycle);
    }

    public void handleTrackLifecycleStatusUpdated(TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent event) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(event.getId(), TrackAdminDoc.class);
        if (adminDoc == null) return;

        LifecycleStatus newLifecycle = LifecycleStatus.valueOf(event.getLifecycleStatus().name());
        adminDoc.setLifecycleStatus(newLifecycle);
        trackIndexRepository.index(adminDoc);

        if (newLifecycle == LifecycleStatus.PUBLISHED) {
            publishTrack(adminDoc);
        }

        log.info("Track lifecycle updated: id={}, lifecycle={}", event.getId(), newLifecycle);
    }

    public void handleArtistAddedToTrack(ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent event) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(event.getTrackId(), TrackAdminDoc.class);
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
        trackIndexRepository.index(adminDoc);

        TrackPublicDoc publicDoc = trackIndexRepository.get(event.getTrackId(), TrackPublicDoc.class);
        if (publicDoc != null) {
            if (publicDoc.getArtists() == null) {
                publicDoc.setArtists(new ArrayList<>());
            }
            publicDoc.getArtists().add(nested);
            trackIndexRepository.index(publicDoc);
        }

        if (artistDoc != null) {
            NestedTrack nestedTrack = new NestedTrack();
            nestedTrack.setId(adminDoc.getId());
            nestedTrack.setTitle(adminDoc.getTitle());

            if (artistDoc.getTracks() == null) {
                artistDoc.setTracks(new ArrayList<>());
            }
            artistDoc.getTracks().add(nestedTrack);
            artistIndexRepository.index(artistDoc);

            ArtistPublicDoc artistPublicDoc = artistIndexRepository.get(event.getArtistId(), ArtistPublicDoc.class);
            if (artistPublicDoc != null) {
                if (artistPublicDoc.getTracks() == null) {
                    artistPublicDoc.setTracks(new ArrayList<>());
                }
                artistPublicDoc.getTracks().add(nestedTrack);
                artistIndexRepository.index(artistPublicDoc);
            }
        }

        log.info("Artist added to track: artistId={}, trackId={}", event.getArtistId(), event.getTrackId());
    }

    public void handleArtistDeletedFromTrack(ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent event) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(event.getTrackId(), TrackAdminDoc.class);
        if (adminDoc == null) return;

        if (adminDoc.getArtists() != null) {
            adminDoc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
        }
        trackIndexRepository.index(adminDoc);

        TrackPublicDoc publicDoc = trackIndexRepository.get(event.getTrackId(), TrackPublicDoc.class);
        if (publicDoc != null && publicDoc.getArtists() != null) {
            publicDoc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
            trackIndexRepository.index(publicDoc);
        }

        ArtistAdminDoc artistDoc = artistIndexRepository.get(event.getArtistId(), ArtistAdminDoc.class);
        if (artistDoc != null) {
            if (artistDoc.getTracks() != null) {
                artistDoc.getTracks().removeIf(t -> t.getId().equals(event.getTrackId()));
            }
            artistIndexRepository.index(artistDoc);

            ArtistPublicDoc artistPublicDoc = artistIndexRepository.get(event.getArtistId(), ArtistPublicDoc.class);
            if (artistPublicDoc != null) {
                if (artistPublicDoc.getTracks() != null) {
                    artistPublicDoc.getTracks().removeIf(t -> t.getId().equals(event.getTrackId()));
                }
                artistIndexRepository.index(artistPublicDoc);
            }
        }

        log.info("Artist removed from track: artistId={}, trackId={}", event.getArtistId(), event.getTrackId());
    }

    public void handleOtherAddedToTrack(OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent event) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(event.getTrackId(), TrackAdminDoc.class);
        if (adminDoc == null) return;

        NestedArtist other = new NestedArtist();
        other.setId(event.getOtherId());
        other.setName(event.getName());

        if (adminDoc.getArtists() == null) {
            adminDoc.setArtists(new ArrayList<>());
        }
        adminDoc.getArtists().add(other);
        trackIndexRepository.index(adminDoc);

        TrackPublicDoc publicDoc = trackIndexRepository.get(event.getTrackId(), TrackPublicDoc.class);
        if (publicDoc != null) {
            if (publicDoc.getArtists() == null) {
                publicDoc.setArtists(new ArrayList<>());
            }
            publicDoc.getArtists().add(other);
            trackIndexRepository.index(publicDoc);
        }

        log.info("Other added to track: otherId={}, trackId={}", event.getOtherId(), event.getTrackId());
    }

    public void handleOtherDeletedFromTrack(OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent event) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(event.getTrackId(), TrackAdminDoc.class);
        if (adminDoc == null) return;

        if (adminDoc.getArtists() != null) {
            adminDoc.getArtists().removeIf(a -> a.getId().equals(event.getOtherId()));
        }
        trackIndexRepository.index(adminDoc);

        TrackPublicDoc publicDoc = trackIndexRepository.get(event.getTrackId(), TrackPublicDoc.class);
        if (publicDoc != null && publicDoc.getArtists() != null) {
            publicDoc.getArtists().removeIf(a -> a.getId().equals(event.getOtherId()));
            trackIndexRepository.index(publicDoc);
        }

        log.info("Other removed from track: otherId={}, trackId={}", event.getOtherId(), event.getTrackId());
    }

    public void handleTrackAddedToAlbum(TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent event) {
        AlbumAdminDoc albumDoc = albumIndexRepository.get(event.getAlbumId(), AlbumAdminDoc.class);
        if (albumDoc == null) return;

        NestedAlbum nestedAlbum = new NestedAlbum();
        nestedAlbum.setId(albumDoc.getId());
        nestedAlbum.setTitle(albumDoc.getTitle());

        TrackAdminDoc adminDoc = trackIndexRepository.get(event.getTrackId(), TrackAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAlbum(nestedAlbum);
            trackIndexRepository.index(adminDoc);

            TrackPublicDoc publicDoc = trackIndexRepository.get(event.getTrackId(), TrackPublicDoc.class);
            if (publicDoc != null) {
                publicDoc.setAlbum(nestedAlbum);
                trackIndexRepository.index(publicDoc);
            }
        }

        if (albumDoc.getTracks() == null) {
            albumDoc.setTracks(new ArrayList<>());
        }
        NestedTrack nestedTrack = new NestedTrack();
        nestedTrack.setId(event.getTrackId());
        nestedTrack.setTitle(adminDoc != null ? adminDoc.getTitle() : "");
        albumDoc.getTracks().add(nestedTrack);
        albumIndexRepository.index(albumDoc);

        AlbumPublicDoc albumPublicDoc = albumIndexRepository.get(event.getAlbumId(), AlbumPublicDoc.class);
        if (albumPublicDoc != null) {
            if (albumPublicDoc.getTracks() == null) {
                albumPublicDoc.setTracks(new ArrayList<>());
            }
            albumPublicDoc.getTracks().add(nestedTrack);
            albumIndexRepository.index(albumPublicDoc);
        }

        log.info("Track added to album: trackId={}, albumId={}", event.getTrackId(), event.getAlbumId());
    }

    public void handleTrackDeleted(String id) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(id, TrackAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.DELETED);
            trackIndexRepository.index(adminDoc);
        }

        trackIndexRepository.delete(id, TrackPublicDoc.class);
        log.info("Track deleted: id={}", id);
    }

    public void handleTrackHidden(String id) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(id, TrackAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.HIDDEN);
            trackIndexRepository.index(adminDoc);
        }

        TrackPublicDoc publicDoc = trackIndexRepository.get(id, TrackPublicDoc.class);
        if (publicDoc != null) {
            publicDoc.setAvailability(EntityStatus.HIDDEN);
            trackIndexRepository.index(publicDoc);
        }

        log.info("Track hidden: id={}", id);
    }

    public void handleTrackActivated(String id) {
        TrackAdminDoc adminDoc = trackIndexRepository.get(id, TrackAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.ACTIVE);
            trackIndexRepository.index(adminDoc);

            TrackPublicDoc publicDoc = trackIndexRepository.get(id, TrackPublicDoc.class);
            if (publicDoc != null) {
                publicDoc.setAvailability(EntityStatus.ACTIVE);
                trackIndexRepository.index(publicDoc);
            } else if (adminDoc.getLifecycleStatus() == LifecycleStatus.PUBLISHED) {
                publishTrack(adminDoc);
            }
        }
        log.info("Track activated: id={}", id);
    }

    private void publishTrack(TrackAdminDoc adminDoc) {
        TrackPublicDoc publicDoc = toPublicDoc(adminDoc);
        trackIndexRepository.index(publicDoc);
        log.info("Track published to public index: id={}", adminDoc.getId());
    }

    public void handleTrackTrueDeleted(String id) {
        trackIndexRepository.delete(id, TrackAdminDoc.class);
        trackIndexRepository.delete(id, TrackPublicDoc.class);
        log.info("Track permanently deleted: id={}", id);
    }

    private TrackPublicDoc toPublicDoc(TrackAdminDoc adminDoc) {
        TrackPublicDoc publicDoc = new TrackPublicDoc();
        publicDoc.setId(adminDoc.getId());
        publicDoc.setTitle(adminDoc.getTitle());
        publicDoc.setAvailability(adminDoc.getAvailability());
        publicDoc.setLifecycleStatus(adminDoc.getLifecycleStatus());
        publicDoc.setArtists(adminDoc.getArtists());
        publicDoc.setAlbum(adminDoc.getAlbum());
        return publicDoc;
    }
}
