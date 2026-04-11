package org.ultra.rcrs.catalogservice.repository;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.read.ArtistOnTrackView;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@ReadingConverter
public class ArtistOnTrackViewReadConverter implements Converter<Json, List<ArtistOnTrackView>> {

    private final ObjectMapper objectMapper;

    public ArtistOnTrackViewReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ArtistOnTrackView> convert(Json source) {
        try {
            return objectMapper.readValue(source.asString(), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}