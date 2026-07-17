package org.ultra.rcrs.mediaservice.service.transcoding;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AudioProcessData {

    private String inputBucket;
    private String inputKey;
    private String outputBucket;
    private UUID guid;
    private String contentType;
    private String codec;
    private String container;
    private String bitrate;
    private int sampleRate;
    private ProcessBuilder processBuilder;
    private String trackId;
    private boolean setDefault;
}
