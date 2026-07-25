package org.ultra.rcrs.playlistservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistTrack {

    private String trackId;
    private int position;
    private LocalDateTime addedAt;
}
