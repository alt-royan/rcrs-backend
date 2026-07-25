package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.mediaservice.config.ImageConfigurationProperties;
import org.ultra.rcrs.mediaservice.dto.ImageResponse;
import org.ultra.rcrs.mediaservice.temporal.activity.model.ValidatedImage;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageUploadService {

    private final ImageValidator imageValidator;
    private final ThumbnailGenerator thumbnailGenerator;
    private final ImageStorage imageStorage;
    private final ImageConfigurationProperties imageProperties;

    public ImageResponse uploadImage(String dataUrl) {
        ValidatedImage validated = imageValidator.validate(dataUrl);
        String key = validated.key();
        byte[] imageData = validated.imageData();
        String contentType = validated.contentType();

        String uri = imageStorage.putImage(key, imageData, contentType);

        for (int size : imageProperties.getThumbnails().getSizes()) {
            String thumbnailKey = key + String.format("/%sx%s", size, size);
            byte[] thumbnail = thumbnailGenerator.createThumbnail(imageData, validated.format(), size);
            imageStorage.putImage(thumbnailKey, thumbnail, contentType);
        }

        log.info("Image upload completed, uri={}", uri);
        return new ImageResponse(uri);
    }
}
