package org.ultra.rcrs.catalogservice.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.regex.Pattern;

@Component
public class S3Utils {

    @Value("${s3.images.endpoint}")
    private String s3ImagesEndpoint;

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
            return "";
        }
        return URI.create(s3ImagesEndpoint + "/" + imageKey).toString();
    }


}
