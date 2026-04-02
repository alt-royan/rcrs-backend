package org.ultra.rcrs.catalogservice.dto.request;

import lombok.Data;
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