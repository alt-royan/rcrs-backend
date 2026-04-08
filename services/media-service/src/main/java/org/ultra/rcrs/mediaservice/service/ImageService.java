package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;
import org.ultra.rcrs.mediaservice.utils.Hash;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageService {

    private final static String CONTENT_TYPE_PREFIX = "data:";
    private final static String ENCODING_PREFIX = ";base64,";
    private final static String[] MIME_TYPES = {"image/jpeg", "image/png"};

    private final S3Client s3Client;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    @Value("${thumbnail.sizes}")
    private int[] thumbnailSizes;

    @Value("${s3.image-bucket}")
    private String s3ImageBucket;

    public String uploadImage(String dataUrl) {
        if (!dataUrl.startsWith(CONTENT_TYPE_PREFIX) || !dataUrl.contains(ENCODING_PREFIX)) {
            throw new BadRequestException("It is not data url string");
        }
        int contentTypeStartIndex = dataUrl.indexOf(CONTENT_TYPE_PREFIX) + CONTENT_TYPE_PREFIX.length();
        int contentTypeEndIndex = dataUrl.indexOf(ENCODING_PREFIX);
        int contentStartIndex = dataUrl.indexOf(ENCODING_PREFIX) + ENCODING_PREFIX.length();

        try {
            String contentType = dataUrl.substring(contentTypeStartIndex, contentTypeEndIndex);

            if (!Arrays.asList(MIME_TYPES).contains(contentType)) {
                throw new BadRequestException("Wrong image mime type");
            }

            String format = contentType.split("/")[1];

            byte[] imageData = Base64.getMimeDecoder().decode(dataUrl.substring(contentStartIndex));
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImage == null) {
                throw new BadRequestException("Unable to decode image. Try another one");
            }

            if (originalImage.getWidth() != originalImage.getHeight()) {
                throw new BadRequestException("Image must be square");
            }

            Map<Integer, ByteArrayOutputStream> thumbnails = new HashMap<>();
            for (int size : thumbnailSizes) {
                thumbnails.put(size, createThumbnail(originalImage, size, format));
            }
            ByteArrayOutputStream ogImageOutputStream = new ByteArrayOutputStream();
            ImageIO.write(originalImage, format, ogImageOutputStream);

            String key = Hash.sha1(ogImageOutputStream.toByteArray());
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(s3ImageBucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(ogImageOutputStream.toByteArray()));

            thumbnails.forEach((size, imageOS) ->
                    CompletableFuture.runAsync(() -> {
                        s3Client.putObject(PutObjectRequest.builder()
                                        .bucket(s3ImageBucket)
                                        .key(key + String.format("/%dx%d", size, size))
                                        .contentType(contentType)
                                        .build(),
                                RequestBody.fromBytes(imageOS.toByteArray()));
                    }, executor).exceptionally(ex -> {
                        log.error("Thumbnail upload failed for size {}", size, ex);
                        return null;
                    })
            );

            return String.format("s3://%s/%s", s3ImageBucket, key);
        } catch (IOException | IllegalArgumentException e) {
            throw new BadRequestException("Unable to decode image. Try another one", e);
        } catch (BadRequestException e) {
            throw e;
        } catch (SdkClientException e) {
            throw new ServiceUnavailableException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private ByteArrayOutputStream createThumbnail(BufferedImage originalImage, int size, String format) throws IOException {
        var resizedImage = Scalr.resize(originalImage, size);
        var baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, format, baos);
        return baos;
    }


}
