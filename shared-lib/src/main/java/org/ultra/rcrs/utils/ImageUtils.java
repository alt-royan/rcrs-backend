package org.ultra.rcrs.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.ultra.rcrs.enums.ImageSize;
import org.ultra.rcrs.exceptions.BadRequestException;

import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ImageUtils {

    private final String cdnImagesEndpoint;
    private final Map<ImageSize, Integer> thumbnailSizes;

    public String parseKey(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return null;
        }
        if (!Pattern.matches("s3://[\\w\\-]+/[\\w\\-.]+", uri)) {
            throw new BadRequestException("URI must be s3://{bucket}/{key} formatted");
        }
        String imageKey = URI.create(uri).getPath();
        if (imageKey.startsWith("/")) imageKey = imageKey.substring(1);
        return imageKey;
    }

    public String parseUrl(String imageKey) {
        if (StringUtils.isEmpty(imageKey)) {
            return null;
        }
        return URI.create(cdnImagesEndpoint + "/" + imageKey).toString();
    }

    /**
     * Resolves every configured thumbnail size for the given stored image key into its
     * public URL, so clients never have to know the storage key suffix scheme or the
     * configured pixel dimensions themselves.
     */
    public Map<ImageSize, URI> parseUrls(String imageKey) {
        if (StringUtils.isEmpty(imageKey)) {
            return Map.of();
        }
        Map<ImageSize, URI> urls = new EnumMap<>(ImageSize.class);
        thumbnailSizes.forEach((size, px) ->
                urls.put(size, URI.create(cdnImagesEndpoint + "/" + imageKey + "/" + px + "x" + px)));
        return urls;
    }

    public Map<ImageSize, Integer> getThumbnailSizes() {
        return thumbnailSizes;
    }
}
