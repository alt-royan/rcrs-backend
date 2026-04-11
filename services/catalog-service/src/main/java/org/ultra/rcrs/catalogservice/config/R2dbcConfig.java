package org.ultra.rcrs.catalogservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.ultra.rcrs.catalogservice.repository.AlbumViewReadConverter;
import org.ultra.rcrs.catalogservice.repository.ArtistOnAlbumViewReadConverter;
import org.ultra.rcrs.catalogservice.repository.ArtistOnTrackViewReadConverter;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions customConversions(ObjectMapper mapper) {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new ArtistOnAlbumViewReadConverter(mapper));
        converters.add(new ArtistOnTrackViewReadConverter(mapper));
        converters.add(new AlbumViewReadConverter(mapper));
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }
}