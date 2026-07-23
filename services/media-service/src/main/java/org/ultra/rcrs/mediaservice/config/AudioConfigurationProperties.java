package org.ultra.rcrs.mediaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Data
@ConfigurationProperties(value = "audio")
public class AudioConfigurationProperties {

    private Validation validation;
    private Loudnorm loudnorm;
    private List<String> bitrates;
    private String codec;
    private String rate;
    private String format;

    @Data
    public static class AudioDuration {
        private Duration min;
        private Duration max;
    }

    @Data
    public static class Validation {
        private List<String> formats;
        private AudioDuration duration;
    }

    @Data
    public static class Loudnorm {
        private Double I;
        private Double LRA;
        private Double TP;
    }
}
