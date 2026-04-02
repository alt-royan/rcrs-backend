package org.ultra.rcrs.catalogservice.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
public class S3Utils {
    private static String S3_URL;

    @Value("${s3.url}")
    public void setS3Url(String s3Url) {
        S3_URL = s3Url;
    }

    public static String createResourceS3Url(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            return new URIBuilder(S3_URL).setPath(key).build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}