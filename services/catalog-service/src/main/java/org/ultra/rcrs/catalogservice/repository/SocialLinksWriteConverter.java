package org.ultra.rcrs.catalogservice.repository;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.SocialLinks;
import tools.jackson.databind.ObjectMapper;

@Component
@WritingConverter
public class SocialLinksWriteConverter implements Converter<SocialLinks, Json> {

    private final ObjectMapper objectMapper;

    public SocialLinksWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Json convert(SocialLinks source) {
        try {
            return Json.of(objectMapper.writeValueAsString(source));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}