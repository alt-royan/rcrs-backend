package org.ultra.rcrs.playlistservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistTrackViewDto {

    private String trackId;
    private int position;
    private LocalDateTime addedAt;
}
