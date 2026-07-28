package org.ultra.rcrs.enums;

/**
 * Named thumbnail sizes generated for every uploaded image. The actual pixel
 * dimension behind each name is configurable (see {@code image.thumbnails.sizes.*}),
 * not hardcoded here, so clients must not assume fixed pixel values.
 */
public enum ImageSize {
    SM,
    MD,
    LG
}
