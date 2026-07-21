package org.ultra.rcrs.mediaservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.dto.PreloadFileRequest;
import org.ultra.rcrs.mediaservice.dto.S3PresignUrlResponse;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioServiceTest {

    @Mock
    private AudioUploadRepository audioUploadRepository;
    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private AudioService audioService;

    @Test
    void getPreSignUrl() throws MalformedURLException {
        ReflectionTestUtils.setField(audioService, "s3UploadBucket", "test-bucket");
        ReflectionTestUtils.setField(audioService, "duration", Duration.ofHours(1));

        var request = new PreloadFileRequest();
        request.setName("song.mp3");
        request.setLength(5_000_000L);

        var httpRequest = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.PUT)
                .uri(URI.create("https://test-bucket.s3.amazonaws.com/test-key"))
                .putHeader("host", "test-bucket.s3.amazonaws.com")
                .putHeader("x-amz-server-side-encryption", "AES256")
                .build();

        var presignedResponse = mock(PresignedPutObjectRequest.class);
        when(presignedResponse.url()).thenReturn(URI.create("https://test-bucket.s3.amazonaws.com/test-key?X-Amz-Signature=abc").toURL());
        when(presignedResponse.httpRequest()).thenReturn(httpRequest);
        when(presignedResponse.signedHeaders()).thenReturn(Map.of(
                "host", List.of("test-bucket.s3.amazonaws.com"),
                "x-amz-server-side-encryption", List.of("AES256")
        ));

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedResponse);

        S3PresignUrlResponse response = audioService.getPreSignUrl(request);

        assertNotNull(response);
        assertNotNull(response.getUid());
        assertEquals("PUT", response.getMethod());
        assertTrue(response.getUrl().contains("X-Amz-Signature"));

        verify(audioUploadRepository).save(argThat(upload ->
                upload.getStatus() == FileStatus.WAIT_FOR_UPLOAD
                        && upload.getOriginalFileName().equals("song.mp3")
                        && upload.getContentLength() == 5_000_000L
        ));
    }

    @Test
    void getPreSignUrlWithHeaders() throws MalformedURLException {
        ReflectionTestUtils.setField(audioService, "s3UploadBucket", "bucket");
        ReflectionTestUtils.setField(audioService, "duration", Duration.ofHours(1));

        var request = new PreloadFileRequest();
        request.setName("track.wav");
        request.setLength(10_000_000L);

        var httpRequest = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.PUT)
                .uri(URI.create("https://bucket.s3.amazonaws.com/key"))
                .build();

        var presignedResponse = mock(PresignedPutObjectRequest.class);
        when(presignedResponse.url()).thenReturn(URI.create("https://bucket.s3.amazonaws.com/key?sig=xyz").toURL());
        when(presignedResponse.httpRequest()).thenReturn(httpRequest);
        when(presignedResponse.signedHeaders()).thenReturn(Map.of());

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedResponse);

        S3PresignUrlResponse response = audioService.getPreSignUrl(request);

        assertNotNull(response);
        assertNotNull(response.getHeaders());
    }

    @Test
    void getPreSignUrlPassesCorrectBucketToPresigner() throws MalformedURLException {
        ReflectionTestUtils.setField(audioService, "s3UploadBucket", "my-uploads");
        ReflectionTestUtils.setField(audioService, "duration", Duration.ofHours(2));

        var request = new PreloadFileRequest();
        request.setName("audio.flac");
        request.setLength(1_000_000L);

        var httpRequest = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.PUT)
                .uri(URI.create("https://my-uploads.s3.amazonaws.com/key"))
                .build();

        var presignedResponse = mock(PresignedPutObjectRequest.class);
        when(presignedResponse.url()).thenReturn(URI.create("https://my-uploads.s3.amazonaws.com/key?sig=abc").toURL());
        when(presignedResponse.httpRequest()).thenReturn(httpRequest);
        when(presignedResponse.signedHeaders()).thenReturn(Map.of());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedResponse);

        audioService.getPreSignUrl(request);

        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }
}
