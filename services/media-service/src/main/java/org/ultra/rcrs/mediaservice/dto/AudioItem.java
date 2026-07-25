package org.ultra.rcrs.mediaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AudioItem {

    private UUID id;

    private UUID guid;

    private String downloadUrl;

    private String codec;

    private String container;

    private Integer durationMs;

    private String bitrate;

    private String sampleRate;

    private Long byteSize;

    private Boolean main;
}
