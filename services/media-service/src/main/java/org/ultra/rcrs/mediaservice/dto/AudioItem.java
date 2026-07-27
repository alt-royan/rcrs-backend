package org.ultra.rcrs.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(description = "A single stored audio asset: one transcoded/encoded variant of an uploaded audio file.")
public class AudioItem {

    @Schema(description = "Unique identifier of this specific audio asset/variant")
    private UUID id;

    @Schema(description = "Identifier shared by all variants that were produced from the same original upload")
    private UUID guid;

    @Schema(description = "Storage key/path of the asset in the underlying object store (e.g. S3)")
    private String key;

    @Schema(description = "Audio codec used to encode this variant (e.g. aac, opus, mp3)")
    private String codec;

    @Schema(description = "Container/format of the encoded file (e.g. mp4, ogg, mp3)")
    private String container;

    @Schema(description = "Duration of the audio in milliseconds")
    private Integer durationMs;

    @Schema(description = "Bitrate of this encoded variant, as a display string (e.g. \"128k\")")
    private String bitrate;

    @Schema(description = "Sample rate of this encoded variant, as a display string (e.g. \"44100\")")
    private String sampleRate;

    @Schema(description = "Size of the encoded file in bytes")
    private Long byteSize;

    @Schema(description = "Whether this variant is the primary/default one to serve for its guid")
    private Boolean main;
}
