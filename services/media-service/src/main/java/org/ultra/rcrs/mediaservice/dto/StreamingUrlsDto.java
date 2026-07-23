package org.ultra.rcrs.mediaservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StreamingUrlsDto {
    private List<String> urls;
}
