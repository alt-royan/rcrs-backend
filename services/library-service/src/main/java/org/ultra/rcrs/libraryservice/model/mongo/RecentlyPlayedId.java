package org.ultra.rcrs.libraryservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentlyPlayedId implements Serializable {

    private String userId;

    private LibraryEntityType entityType;

    private String entityId;
}
