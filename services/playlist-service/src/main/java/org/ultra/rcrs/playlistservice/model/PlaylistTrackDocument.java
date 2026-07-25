package org.ultra.rcrs.playlistservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "playlist_tracks")
@CompoundIndexes({
        @CompoundIndex(name = "playlist_position_idx", def = "{'playlistId': 1, 'position': 1}"),
        @CompoundIndex(name = "playlist_track_idx", def = "{'playlistId': 1, 'trackId': 1}", unique = true)
})
public class PlaylistTrackDocument {

    @Id
    private String id;
    private String playlistId;
    private String trackId;
    private int position;
    private LocalDateTime addedAt;
}
