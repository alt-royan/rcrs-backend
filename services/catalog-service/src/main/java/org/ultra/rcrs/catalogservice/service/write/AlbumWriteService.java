package org.ultra.rcrs.catalogservice.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.dto.response.IdResponse;
import org.ultra.rcrs.catalogservice.kafka.producer.EventProducer;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.model.write.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.repository.AfterCommit;
import org.ultra.rcrs.catalogservice.repository.write.impl.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.write.impl.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.write.impl.ArtistToAlbumRepository;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
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
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumWriteService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final TrackWriteService trackWriteService;
    private final S3Utils s3Utils;
    private final EventProducer eventProducer;

    @Transactional
    public Mono<IdResponse> createAlbum(AlbumUploadRequest request) {
        if (request.getTracks() != null && !request.getTracks().isEmpty()) {
            Set<Integer> numbers = request.getTracks().stream()
                    .map(TrackUploadRequest::getTrackNumber)
                    .collect(Collectors.toSet());

            int size = request.getTracks().size();

            if (numbers.size() != size || !IntStream.rangeClosed(1, size).allMatch(numbers::contains)) {
                throw new BadRequestException("trackNumbers are wrong");
            }

            request.getTracks().forEach(t -> t.setReleaseDate(request.getReleaseDate()));
        }

        UUID albumId = UUID.randomUUID();
        return checkArtists(request.getArtists())
                .then(albumRepository.insert(Album.builder()
                                .id(albumId)
                                .status(EntityStatus.CREATED)
                                .title(request.getTitle())
                                .type(request.getType())
                                .releaseDate(request.getReleaseDate())
                                .coverS3Key(s3Utils.parseKey(request.getCoverUri()))
                                .available(true)
                                .build())
                        .flatMap(a -> AfterCommit.log("Album {} saved with id {}", request.getTitle(), albumId)
                                .thenMany(saveArtistsToAlbum(request.getArtists(), a.getId()))
                                .thenMany(trackWriteService.createTracks(request.getTracks(), a.getId()))
                                .then(AfterCommit.execute(() -> eventProducer.albumCreated(a.getId())))
                                .thenReturn(new IdResponse(Url62.encode(a.getId())))));
    }

    private Mono<Void> checkArtists(List<ArtistIdDto> artists) {
        return Flux.fromIterable(artists)
                .flatMap(a -> {
                    var id = Url62.decode(a.getId());
                    return artistRepository.existsById(id)
                            .flatMap(exists -> {
                                if (!exists)
                                    return Mono.error(new NotFoundException("Artist", id));
                                return Mono.empty();
                            });
                }).then();
    }

    private Flux<Void> saveArtistsToAlbum(List<ArtistIdDto> artists, UUID albumId) {
        return Flux.fromIterable(artists)
                .map(a -> ArtistToAlbum.builder()
                        .artistId(Url62.decode(a.getId()))
                        .albumId(albumId)
                        .artistRole(a.getRole())
                        .build())
                .flatMap(artistToAlbumRepository::insert)
                .flatMap(a -> AfterCommit.log("Artist {} with role {} attached to album {}",
                        a.getArtistId(), a.getArtistRole(), a.getAlbumId()));
    }

}
