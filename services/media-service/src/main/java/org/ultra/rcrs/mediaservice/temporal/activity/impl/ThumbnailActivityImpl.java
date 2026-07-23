package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.temporal.activity.ThumbnailActivity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@ActivityImpl
@Slf4j
public class ThumbnailActivityImpl implements ThumbnailActivity {

    @Override
    public byte[] createThumbnail(byte[] imageData, String format, int size) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            BufferedImage resized = Scalr.resize(originalImage, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, format, baos);

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed when try to create thumbnail", e);
            throw new RuntimeException(e);
        }
    }
}
