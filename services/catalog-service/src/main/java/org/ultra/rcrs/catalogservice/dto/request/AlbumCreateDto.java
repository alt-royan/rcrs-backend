package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.ImageDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.simplify.ArtistSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.enums.AlbumType;

import java.time.LocalDate;
import java.util.List;

@Data
public class AlbumCreateDto {

    private String title;

    private AlbumType albumType;

    private LocalDate releaseDate;

    private String imageKey;

    private List<String> artistIds;

}