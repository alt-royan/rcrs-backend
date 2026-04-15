package org.ultra.rcrs.catalogservice.repository;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.SocialLinks;
import tools.jackson.databind.ObjectMapper;

@Component
@ReadingConverter
public class SocialLinksReadConverter implements Converter<Json, SocialLinks> {

    private final ObjectMapper objectMapper;

    public SocialLinksReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SocialLinks convert(Json source) {
        try {
            return objectMapper.readValue(source.asString(), SocialLinks.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}