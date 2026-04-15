package org.ultra.rcrs.catalogservice.dto.response.artist;

import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;

import java.util.List;

@Data
@Builder
public class ArtistDto {

    private String id;

    private String name;

    private String avatarUrl;

    private List<SocialLinkDto> socialLinks;
}