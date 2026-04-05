package org.ultra.rcrs.catalogservice.utils;

import org.springframework.util.PatternMatchUtils;

import java.net.URI;

public class CoreUtils {

    public static String getKey(String uri) {
        if(!PatternMatchUtils.simpleMatch("s3://[\\w\\-]+/[\\w\\-.]+", uri)){
            throw new IllegalArgumentException("URI must be s3://{bucket}/{key} formatted");
        }
        String imageKey = URI.create(uri).getPath();
        if (imageKey.startsWith("/")) imageKey = imageKey.substring(1);
        return imageKey;
    }

}
