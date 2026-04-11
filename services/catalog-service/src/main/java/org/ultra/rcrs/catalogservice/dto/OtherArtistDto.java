package org.ultra.rcrs.catalogservice.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.catalogservice.model.read.OtherArtistView;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.Set;

@Data
@NoArgsConstructor
public class OtherArtistDto {

    private String name;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Set<ArtistRole> roles;

    public OtherArtistDto(OtherArtistView other) {
        this.name = other.getName();
        this.roles = other.getRoles();
    }
}