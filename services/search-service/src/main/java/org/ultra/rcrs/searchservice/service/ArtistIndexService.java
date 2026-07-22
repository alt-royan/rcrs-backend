package org.ultra.rcrs.searchservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.searchservice.document.ArtistAdminDoc;
import org.ultra.rcrs.searchservice.document.ArtistPublicDoc;
import org.ultra.rcrs.searchservice.repository.ArtistIndexRepository;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistIndexService {

    private final ArtistIndexRepository artistIndexRepository;

    public void handleArtistCreated(ArtistCreatedEventOuterClass.ArtistCreatedEvent event) {
        EntityStatus status = EntityStatus.valueOf(event.getAvailabilityStatus().name());

        ArtistAdminDoc adminDoc = new ArtistAdminDoc();
        adminDoc.setId(event.getId());
        adminDoc.setName(event.getName());
        adminDoc.setTags(event.getTagsList());
        adminDoc.setAvailability(status);
        adminDoc.setAlbums(new ArrayList<>());
        adminDoc.setTracks(new ArrayList<>());
        artistIndexRepository.index(adminDoc);

        if (isPublic(status)) {
            ArtistPublicDoc publicDoc = toPublicDoc(adminDoc);
            artistIndexRepository.index(publicDoc);
        }

        log.info("Artist indexed: id={}, availability={}", event.getId(), status);
    }

    public void handleArtistDeleted(String id) {
        ArtistAdminDoc adminDoc = artistIndexRepository.get(id, ArtistAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.DELETED);
            artistIndexRepository.index(adminDoc);
        }

        artistIndexRepository.delete(id, ArtistPublicDoc.class);
        log.info("Artist deleted: id={}", id);
    }

    public void handleArtistHidden(String id) {
        ArtistAdminDoc adminDoc = artistIndexRepository.get(id, ArtistAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.HIDDEN);
            artistIndexRepository.index(adminDoc);
        }

        ArtistPublicDoc publicDoc = artistIndexRepository.get(id, ArtistPublicDoc.class);
        if (publicDoc != null) {
            publicDoc.setAvailability(EntityStatus.HIDDEN);
            artistIndexRepository.index(publicDoc);
        }

        log.info("Artist hidden: id={}", id);
    }

    public void handleArtistActivated(String id) {
        ArtistAdminDoc adminDoc = artistIndexRepository.get(id, ArtistAdminDoc.class);
        if (adminDoc != null) {
            adminDoc.setAvailability(EntityStatus.ACTIVE);
            artistIndexRepository.index(adminDoc);

            ArtistPublicDoc publicDoc = artistIndexRepository.get(id, ArtistPublicDoc.class);
            if (publicDoc != null) {
                publicDoc.setAvailability(EntityStatus.ACTIVE);
            } else {
                publicDoc = toPublicDoc(adminDoc);
            }
            artistIndexRepository.index(publicDoc);
        }

        log.info("Artist activated: id={}", id);
    }

    private ArtistPublicDoc toPublicDoc(ArtistAdminDoc adminDoc) {
        ArtistPublicDoc publicDoc = new ArtistPublicDoc();
        publicDoc.setId(adminDoc.getId());
        publicDoc.setName(adminDoc.getName());
        publicDoc.setTags(adminDoc.getTags());
        publicDoc.setAlbums(adminDoc.getAlbums());
        publicDoc.setTracks(adminDoc.getTracks());
        publicDoc.setAvailability(adminDoc.getAvailability());
        return publicDoc;
    }

    public void handleArtistTrueDeleted(String id) {
        artistIndexRepository.delete(id, ArtistAdminDoc.class);
        artistIndexRepository.delete(id, ArtistPublicDoc.class);
        log.info("Artist permanently deleted: id={}", id);
    }

    private boolean isPublic(EntityStatus availability) {
        return availability == EntityStatus.ACTIVE || availability == EntityStatus.HIDDEN;
    }
}
