package org.ultra.rcrs.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class S3Utils {

    private final String s3ImagesEndpoint;

    public String parseKey(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return null;
        }
        if (!Pattern.matches("s3://[\\w\\-]+/[\\w\\-.]+", uri)) {
            throw new IllegalArgumentException("URI must be s3://{bucket}/{key} formatted");
        }
        String imageKey = URI.create(uri).getPath();
        if (imageKey.startsWith("/")) imageKey = imageKey.substring(1);
        return imageKey;
    }

    public String parseUrl(String imageKey) {
        if (StringUtils.isEmpty(imageKey)) {
            return null;
        }
        return URI.create(s3ImagesEndpoint + "/" + imageKey).toString();
    }

}