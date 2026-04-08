package org.ultra.rcrs.mediaservice.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(S3ConfigProperties.class)
public class S3Config {

    @Bean
    public S3Client amazonS3(S3ConfigProperties s3ConfigProperties) {
        AwsCredentials credentials = AwsBasicCredentials.create(s3ConfigProperties.getAccessKey(), s3ConfigProperties.getSecretKey());
        return S3Client
                .builder()
                .endpointOverride(URI.create(s3ConfigProperties.getUrl()))
                .region(Region.of(s3ConfigProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .overrideConfiguration(b -> b
                        .apiCallTimeout(Duration.ofSeconds(10))
                        .apiCallAttemptTimeout(Duration.ofSeconds(4))
                        .retryStrategy(RetryMode.STANDARD))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Client s3Client, S3ConfigProperties s3ConfigProperties) {
        AwsCredentials credentials = AwsBasicCredentials.create(s3ConfigProperties.getAccessKey(), s3ConfigProperties.getSecretKey());
        return S3Presigner.builder().endpointOverride(URI.create(s3ConfigProperties.getUrl()))
                .region(Region.of(s3ConfigProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .s3Client(s3Client)
                .build();
    }

    @Bean
    public S3Utilities s3Utilities(S3Client s3Client) {
        return s3Client.utilities();
    }
}