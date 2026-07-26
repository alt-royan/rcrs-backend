package org.ultra.rcrs.mediaservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.mediaservice.temporal.activity.model.ValidatedImage;
import org.ultra.rcrs.mediaservice.utils.Hash;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;

@Component
@Slf4j
public class ImageValidator {

    private static final String CONTENT_TYPE_PREFIX = "data:";
    private static final String ENCODING_PREFIX = ";base64,";
    private static final String[] MIME_TYPES = {"image/jpeg", "image/png"};

    public ValidatedImage validate(String dataUrl) {
        if (!dataUrl.startsWith(CONTENT_TYPE_PREFIX) || !dataUrl.contains(ENCODING_PREFIX)) {
            throw new BadRequestException("It is not data url string");
        }
        int contentTypeStartIndex = dataUrl.indexOf(CONTENT_TYPE_PREFIX) + CONTENT_TYPE_PREFIX.length();
        int contentTypeEndIndex = dataUrl.indexOf(ENCODING_PREFIX);
        int contentStartIndex = dataUrl.indexOf(ENCODING_PREFIX) + ENCODING_PREFIX.length();

        String contentType = dataUrl.substring(contentTypeStartIndex, contentTypeEndIndex);

        if (!Arrays.asList(MIME_TYPES).contains(contentType)) {
            throw new UnsupportedMediaTypeStatusException("Wrong image mime type");
        }

        String format = contentType.split("/")[1];
        byte[] imageData;
        try {
            imageData = Base64.getMimeDecoder().decode(dataUrl.substring(contentStartIndex));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Image payload is not valid base64", e);
        }

        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (Exception e) {
            throw new BadRequestException("Unable to decode image. Try another one", e);
        }
        if (originalImage == null) {
            throw new BadRequestException("Unable to decode image. Try another one");
        }

        if (originalImage.getWidth() != originalImage.getHeight()) {
            throw new BadRequestException("Image must be square");
        }

        String key = Hash.sha1(imageData);

        log.info("Image validated: format={}, key={}", format, key);
        return new ValidatedImage(format, contentType, key, imageData);
    }
}
