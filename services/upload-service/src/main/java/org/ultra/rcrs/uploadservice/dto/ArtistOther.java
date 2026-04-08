package org.ultra.rcrs.uploadservice.dto;

import lombok.Data;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.Set;

@Data
public class ArtistOther {


    private String name;


    private SocialLink socialLink;

    private Set<ArtistRole> roles;
}