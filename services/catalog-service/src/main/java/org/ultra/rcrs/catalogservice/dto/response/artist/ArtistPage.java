package org.ultra.rcrs.catalogservice.dto.response.artist;

import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.model.SocialLink;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ArtistPage {

    private String id;

    private String name;

    private List<SocialLink> socialLinks;

    private String avatarUrl;
}