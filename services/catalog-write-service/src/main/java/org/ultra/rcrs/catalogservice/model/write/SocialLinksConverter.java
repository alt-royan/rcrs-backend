package org.ultra.rcrs.catalogservice.model.write;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.ultra.rcrs.catalogservice.model.SocialLinks;
import tools.jackson.databind.ObjectMapper;

@Converter
public class SocialLinksConverter implements AttributeConverter<SocialLinks, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SocialLinks attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing SocialLinks", e);
        }
    }

    @Override
    public SocialLinks convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return objectMapper.readValue(dbData, SocialLinks.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing SocialLinks", e);
        }
    }
}
