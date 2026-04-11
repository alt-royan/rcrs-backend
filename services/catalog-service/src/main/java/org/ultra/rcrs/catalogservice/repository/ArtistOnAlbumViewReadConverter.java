package org.ultra.rcrs.catalogservice.repository;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.read.ArtistOnAlbumView;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@ReadingConverter
public class ArtistOnAlbumViewReadConverter implements Converter<Json, List<ArtistOnAlbumView>> {

    private final ObjectMapper objectMapper;

    public ArtistOnAlbumViewReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ArtistOnAlbumView> convert(Json source) {
        try {
            return objectMapper.readValue(source.asString(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}