package org.ultra.rcrs.catalogservice.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.catalogservice.dto.response.IdResponse;
import org.ultra.rcrs.catalogservice.model.SocialLinks;
import org.ultra.rcrs.catalogservice.model.write.Artist;
import org.ultra.rcrs.catalogservice.repository.AfterCommit;
import org.ultra.rcrs.catalogservice.repository.write.impl.ArtistRepository;

import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistWriteService {

    private final ArtistRepository artistRepository;
    private final S3Utils s3Utils;

    @Transactional
    public Mono<IdResponse> createArtist(ArtistCreateRequest request) {
        var id = UUID.randomUUID();
        return artistRepository.insert(Artist.builder()
                        .id(id)
                        .name(request.getName())
                        .avatarS3Key(s3Utils.parseKey(request.getAvatarUri()))
                        .socialLinks(new SocialLinks(request.getSocialLinks()))
                        .build())
                .flatMap(a ->
                        AfterCommit.log("Artist {} was created successfully. Artist UUID {}", a.getName(), a.getId()))
                .thenReturn(new IdResponse(Url62.encode(id)));
    }

}
