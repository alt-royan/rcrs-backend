package org.ultra.rcrs.catalogservice.service.read;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumOfArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.model.read.ArtistAlbumView;
import org.ultra.rcrs.catalogservice.model.read.ArtistOnAlbumView;
import org.ultra.rcrs.catalogservice.model.read.ArtistView;
import org.ultra.rcrs.catalogservice.repository.read.ArtistAlbumViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.ArtistViewRepository;
import org.ultra.rcrs.catalogservice.service.ArtistConverter;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistReadServiceTest {

    @Mock
    private ArtistViewRepository artistRepository;

    @Mock
    private ArtistAlbumViewRepository artistAlbumViewRepository;

    @Mock
    private ArtistConverter artistConverter;

    @Mock
    private S3Utils s3Utils;

    private ArtistReadService artistReadService;

    @BeforeEach
    void setUp() {
        artistReadService = new ArtistReadService(artistRepository, artistAlbumViewRepository, artistConverter, s3Utils);
    }

    @Test
    void getArtist_shouldReturnArtistDto_whenArtistExists() {
        UUID artistId = UUID.randomUUID();
        ArtistView artistView = ArtistView.builder()
                .id(artistId)
                .name("Test Artist")
                .avatarS3Key("avatars/test.jpg")
                .build();
        ArtistDto expectedDto = ArtistDto.builder()
                .id("encodedId")
                .name("Test Artist")
                .avatarUrl("http://localhost:9444/avatars/test.jpg")
                .build();

        when(artistRepository.findById(artistId)).thenReturn(Mono.just(artistView));
        when(artistConverter.toDto(artistView)).thenReturn(expectedDto);

        StepVerifier.create(artistReadService.getArtist(artistId))
                .assertNext(dto -> {
                    assertThat(dto.getId()).isEqualTo("encodedId");
                    assertThat(dto.getName()).isEqualTo("Test Artist");
                    assertThat(dto.getAvatarUrl()).isEqualTo("http://localhost:9444/avatars/test.jpg");
                })
                .verifyComplete();
    }

    @Test
    void getArtist_shouldThrowNotFoundException_whenArtistDoesNotExist() {
        UUID artistId = UUID.randomUUID();

        when(artistRepository.findById(artistId)).thenReturn(Mono.empty());

        StepVerifier.create(artistReadService.getArtist(artistId))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("Artist"))
                .verify();
    }

    @Test
    void getAlbumsForArtist_shouldReturnAlbums_whenAlbumsExist() {
        UUID artistId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        List<EntityStatus> statuses = List.of(EntityStatus.PUBLISHED);
        List<ArtistOnAlbumView> artistsOnAlbum = List.of(
                ArtistOnAlbumView.builder()
                        .id(UUID.randomUUID())
                        .name("Featured Artist")
                        .avatarS3Key("avatars/featured.jpg")
                        .role(ArtistRole.MAIN_ARTIST)
                        .build()
        );
        ArtistAlbumView albumView = ArtistAlbumView.builder()
                .albumId(albumId)
                .artistId(artistId)
                .artistRole(ArtistRole.MAIN_ARTIST)
                .status(EntityStatus.PUBLISHED)
                .title("Test Album")
                .type(AlbumType.SINGLE)
                .releaseDate(LocalDate.of(2024, 1, 15))
                .year(2024)
                .totalTracks(5)
                .totalDurationMs(1800000)
                .coverS3Key("covers/test.jpg")
                .explicit(false)
                .available(true)
                .artists(artistsOnAlbum)
                .build();

        when(artistAlbumViewRepository.findAllByArtist(artistId, statuses, null, null, Sort.Direction.DESC))
                .thenReturn(reactor.core.publisher.Flux.just(albumView));
        when(s3Utils.parseUrl("covers/test.jpg")).thenReturn("http://localhost:9444/covers/test.jpg");
        when(artistConverter.onAlbumToDto(artistsOnAlbum)).thenReturn(List.of());

        StepVerifier.create(artistReadService.getAlbumsForArtist(artistId, statuses, null, null, Sort.Direction.DESC))
                .assertNext(albums -> {
                    assertThat(albums).hasSize(1);
                    AlbumOfArtistDto album = albums.get(0);
                    assertThat(album.getTitle()).isEqualTo("Test Album");
                    assertThat(album.getType()).isEqualTo(AlbumType.SINGLE);
                    assertThat(album.getYear()).isEqualTo(2024);
                    assertThat(album.getTotalTracks()).isEqualTo(5);
                    assertThat(album.getCoverUrl()).isEqualTo("http://localhost:9444/covers/test.jpg");
                    assertThat(album.getArtistRole()).isEqualTo(ArtistRole.MAIN_ARTIST);
                    assertThat(album.getStatus()).isEqualTo(EntityStatus.PUBLISHED);
                })
                .verifyComplete();
    }

    @Test
    void getAlbumsForArtist_shouldReturnEmptyList_whenNoAlbumsExist() {
        UUID artistId = UUID.randomUUID();
        List<EntityStatus> statuses = List.of(EntityStatus.PUBLISHED);

        when(artistAlbumViewRepository.findAllByArtist(artistId, statuses, null, null, Sort.Direction.DESC))
                .thenReturn(reactor.core.publisher.Flux.empty());

        StepVerifier.create(artistReadService.getAlbumsForArtist(artistId, statuses, null, null, Sort.Direction.DESC))
                .assertNext(albums -> assertThat(albums).isEmpty())
                .verifyComplete();
    }

    @Test
    void getAlbumsForArtist_shouldFilterByRoleAndType() {
        UUID artistId = UUID.randomUUID();
        List<EntityStatus> statuses = List.of(EntityStatus.PUBLISHED);

        when(artistAlbumViewRepository.findAllByArtist(artistId, statuses, ArtistRole.MAIN_ARTIST, AlbumType.ALBUM, Sort.Direction.ASC))
                .thenReturn(reactor.core.publisher.Flux.empty());

        StepVerifier.create(artistReadService.getAlbumsForArtist(artistId, statuses, ArtistRole.MAIN_ARTIST, AlbumType.ALBUM, Sort.Direction.ASC))
                .assertNext(albums -> assertThat(albums).isEmpty())
                .verifyComplete();
    }
}
