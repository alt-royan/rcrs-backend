package org.ultra.rcrs.libraryservice.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadedCollectionPK implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private LibraryEntityType entityType;

    @Column(name = "entity_id")
    private String entityId;
}
