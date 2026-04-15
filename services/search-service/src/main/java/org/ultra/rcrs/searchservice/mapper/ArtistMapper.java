package org.ultra.rcrs.searchservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.searchservice.document.ArtistDoc;
import org.ultra.rcrs.utils.S3Utils;

@Component
@RequiredArgsConstructor
public class ArtistMapper {

    private final S3Utils s3Utils;

    public ArtistHit toSearchResult(ArtistDoc artist) {
        return ArtistHit.builder()
                .id(artist.getId())
                .name(artist.getName())
                .avatarUrl(s3Utils.parseUrl(artist.getAvatarS3Key()))
                .build();
    }
}
