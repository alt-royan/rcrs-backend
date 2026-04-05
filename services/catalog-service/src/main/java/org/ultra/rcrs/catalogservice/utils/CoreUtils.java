package org.ultra.rcrs.catalogservice.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.regex.Pattern;

public class CoreUtils {

    public static String getKey(String uri) {
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

}
