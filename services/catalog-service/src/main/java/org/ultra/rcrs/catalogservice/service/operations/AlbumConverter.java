package org.ultra.rcrs.catalogservice.service.operations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumInTrack;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumPage;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumStandalone;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbum;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.service.MediaServiceClient;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumConverter {

    private final AlbumRepository albumRepository;
    private final ArtistConverter artistConverter;
    private final MediaServiceClient mediaServiceClient;

    public Mono<AlbumInTrack> collectAlbumForTrack(UUID albumId, List<EntityStatus> albumStatuses) {
        return albumRepository.findByIdAndStatusIn(albumId, albumStatuses).flatMap(album ->
                artistConverter.collectArtistsSimple(album.getMainArtists()).zipWith(mediaServiceClient.fetchImageUrl(album.getCoverS3Key()))
                        .map(tuple -> new AlbumInTrack(album, tuple.getT2(), tuple.getT1())));
    }

    public Mono<AlbumPage> toDto(Album album, List<TrackInAlbum> tracks) {
        return artistConverter.collectArtistsSimple(album.getMainArtists())
                .zipWith(artistConverter.collectArtistsSimple(album.getFeaturedArtists()))
                .zipWith(mediaServiceClient.fetchImageUrl(album.getCoverS3Key()))
                .map(tuple -> new AlbumPage(album, tuple.getT2(), tuple.getT1().getT1(), tuple.getT1().getT2(), tracks));
    }

    public Mono<AlbumStandalone> toDto(AlbumByArtist album) {
        return artistConverter.collectArtistsSimple(album.getMainArtists())
                .zipWith(mediaServiceClient.fetchImageUrl(album.getCoverS3Key()))
                .map(tuple -> new AlbumStandalone(album, tuple.getT2(), tuple.getT1()));
    }

}
