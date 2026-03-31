package org.ultra.rcrs.catalogservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.catalogservice.dto.simplify.ArtistSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.utils.Url62;

import java.time.LocalDate;
import java.util.Collection;

@AllArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({"status", "message"})
public class ErrorResponse {

    private int status;
    private String message;

}