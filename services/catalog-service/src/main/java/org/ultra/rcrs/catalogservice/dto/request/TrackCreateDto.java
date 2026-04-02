package org.ultra.rcrs.catalogservice.dto.request;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;
import org.ultra.rcrs.enums.AlbumType;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class TrackCreateDto {

    private String title;

    private UUID albumId;

    private Set<ArtistWithRole> artists;

    private Integer trackNumber;

    private Boolean explicit;

}