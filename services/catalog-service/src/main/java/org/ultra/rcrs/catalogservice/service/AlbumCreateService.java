package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateDto;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumCreateService {

    private final AlbumRepository albumRepository;

    public Mono<Album> createAlbum(AlbumCreateDto dto) {
        return albumRepository.save(Album.builder()
                .title(dto.getTitle())
                .albumType(dto.getAlbumType())
                .artists()
                .imageKey(dto.getImageKey())
                .releaseDate(dto.getReleaseDate())
                .build());
    }

}
