package org.ultra.rcrs.playlistservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistViewDto {

    private String id;
    private String ownerId;
    private String title;
    private String description;
    private List<String> tags;
    private String coverUrl;
    private Boolean isPublic;
    private Integer trackCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
