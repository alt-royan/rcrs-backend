package org.ultra.rcrs.catalogservice.repository;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.read.AlbumView;
import tools.jackson.databind.ObjectMapper;

@Component
@ReadingConverter
public class AlbumViewReadConverter implements Converter<Json, AlbumView> {

    private final ObjectMapper objectMapper;

    public AlbumViewReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public AlbumView convert(Json source) {
        try {
            return objectMapper.readValue(source.asString(), AlbumView.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}