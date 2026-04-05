package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateRequest;
import org.ultra.rcrs.catalogservice.dto.request.TrackCreateRequest;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumPage;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumStandalone;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.catalogservice.repository.AlbumByArtistRepository;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.catalogservice.service.operations.AlbumConverter;
import org.ultra.rcrs.catalogservice.service.operations.TrackConverter;
import org.ultra.rcrs.catalogservice.utils.CoreUtils;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumCrudService {

    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final AlbumByArtistRepository albumByArtistRepository;
    private final TrackConverter trackConverter;
    private final AlbumConverter albumConverter;

    public Mono<AlbumPage> getAlbum(UUID albumId, List<EntityStatus> statuses) {
        return albumRepository.findByIdAndStatusIn(albumId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .zipWith(trackConverter.collectTracksForAlbum(albumId, statuses))
                .flatMap(tuple -> albumConverter.toDto(tuple.getT1(), tuple.getT2()));
    }

    public Mono<List<AlbumStandalone>> getAlbumsForArtist(UUID artistId, List<EntityStatus> statuses, ArtistRole[] roles, AlbumType[] types, Sort.Direction direction) {
        return albumByArtistRepository.findAll(artistId, statuses, roles, types, direction)
                .flatMap(albumConverter::toDto)
                .collectList();
    }

    public Mono<AlbumPage> createAlbum(AlbumCreateRequest request) {
        Set<Integer> numbers = request.getTracks().stream()
                .map(TrackCreateRequest::getTrackNumber)
                .collect(Collectors.toSet());

        if (numbers.size() != request.getTracks().size()) {
            throw new BadRequestException("trackNumbers are wrong");
        }
        int size = request.getTracks().size();

        for (int i = 1; i <= size; i++) {
            if (!numbers.contains(i)) {
                throw new BadRequestException("trackNumbers are wrong");
            }
        }

        return albumRepository.save(Album.builder()
                .title(request.getTitle())
                .type(request.getType())
                .releaseDate(request.getReleaseDate())
                .coverS3Key(CoreUtils.getKey(request.getCoverUri()))
                .explicit(request.getExplicit())
                .mainArtists(request.getArtists().stream()
                        .map(dto -> Url62.decode(dto.getId()))
                        .collect(Collectors.toSet()))
                .build()
        ).flatMap(album -> Flux.fromIterable(request.getTracks())
                .map(dto -> {
                    Set<UUID> mainArtists = dto.getArtists().stream()
                            .filter(a -> ArtistRole.MAIN_ARTIST.equals(a.getRole()))
                            .map(a -> Url62.decode(a.getId())).collect(Collectors.toSet());
                    Set<UUID> featuredArtists = dto.getArtists().stream()
                            .filter(a -> ArtistRole.FEATURED_ARTIST.equals(a.getRole()))
                            .map(a -> Url62.decode(a.getId())).collect(Collectors.toSet());

                    return Track.builder()
                            .albumId(album.getKey().getId())
                            .title(dto.getTitle())
                            .trackNumber(dto.getTrackNumber())
                            .explicit(dto.getExplicit())
                            .mainArtists(mainArtists)
                            .featuredArtists(featuredArtists)
                            .others(dto.getOthers())
                            .build();
                }).flatMap(t -> trackRepository.save(t).then(incTotalTracks(album.getKey().getId())))
                .then(getAlbum(album.getKey().getId(), List.of(EntityStatus.values()))));

    }

    public Mono<Void> incTotalTracks(UUID albumId) {
        return albumRepository.incTotalTracks(albumId).then(albumByArtistRepository.incTotalTracks(albumId));
    }


}
