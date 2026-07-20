package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.temporal.activity.SaveThumbnailActivity;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
@ActivityImpl
@Slf4j
public class SaveThumbnailActivityImpl implements SaveThumbnailActivity {

    private final S3Client s3Client;

    @Value("${cdn.images.bucket}")
    private String s3ImageBucket;

    public SaveThumbnailActivityImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void saveThumbnail(String key, byte[] imageData, String format, String contentType, int size) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImage == null) {
                throw new RuntimeException("Failed to decode image for thumbnail generation");
            }

            BufferedImage resized = Scalr.resize(originalImage, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, format, baos);

            String thumbnailKey = key + String.format("/%dx%d", size, size);
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(s3ImageBucket)
                            .key(thumbnailKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(baos.toByteArray()));

            log.info("Thumbnail saved: {}/{}", s3ImageBucket, thumbnailKey);
        } catch (Exception e) {
            log.error("Failed to save thumbnail for key={} size={}", key, size, e);
            throw new RuntimeException("Thumbnail generation failed for size=" + size, e);
        }
    }
}
