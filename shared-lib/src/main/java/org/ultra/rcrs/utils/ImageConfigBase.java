package org.ultra.rcrs.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.ultra.rcrs.enums.ImageSize;

import java.util.EnumMap;
import java.util.Map;

/**
 * Shared image/CDN configuration wiring, imported by each service's own
 * {@code @Configuration} class (same pattern as {@link org.ultra.rcrs.kafka.config.KafkaBaseConfig}),
 * so the CDN endpoint and thumbnail size scheme are defined once and stay identical
 * across every service instead of being re-declared per module.
 */
public class ImageConfigBase {

    @Value("${image.cdn.endpoint:}")
    private String cdnImagesEndpoint;

    @Value("${image.thumbnails.sizes.sm:128}")
    private int thumbnailSizeSm;

    @Value("${image.thumbnails.sizes.md:512}")
    private int thumbnailSizeMd;

    @Value("${image.thumbnails.sizes.lg:1024}")
    private int thumbnailSizeLg;

    @Bean
    public ImageUtils imageUtils() {
        Map<ImageSize, Integer> sizes = new EnumMap<>(ImageSize.class);
        sizes.put(ImageSize.SM, thumbnailSizeSm);
        sizes.put(ImageSize.MD, thumbnailSizeMd);
        sizes.put(ImageSize.LG, thumbnailSizeLg);
        return new ImageUtils(cdnImagesEndpoint, sizes);
    }
}
