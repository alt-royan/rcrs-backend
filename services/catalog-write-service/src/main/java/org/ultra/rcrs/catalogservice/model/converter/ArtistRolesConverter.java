package org.ultra.rcrs.catalogservice.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class ArtistRolesConverter implements AttributeConverter<Set<ArtistRole>, String> {

    @Override
    public String convertToDatabaseColumn(Set<ArtistRole> attribute) {
        if (attribute == null || attribute.isEmpty()) return null;
        return attribute.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public Set<ArtistRole> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return Set.of();
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(ArtistRole::valueOf)
                .collect(Collectors.toSet());
    }
}
