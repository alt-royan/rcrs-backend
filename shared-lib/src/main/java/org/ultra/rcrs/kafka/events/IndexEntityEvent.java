package org.ultra.rcrs.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ultra.rcrs.enums.EntityType;

import java.util.Map;

@Data
@AllArgsConstructor
public class IndexEntityEvent {

    public static final String ARTIST_CREATE_SINGLE = "artistCreate:Single";
    public static final String ALBUM_CREATE_SINGLE = "albumCreate:Single";
    public static final String TRACK_CREATE_SINGLE = "trackCreate:Single";

    public static final String ARTIST_CREATE_BATCH = "artistCreate:Batch";
    public static final String ALBUM_CREATE_BATCH = "albumCreate:Batch";
    public static final String TRACK_CREATE_BATCH = "trackCreate:Batch";

    public static final String ARTIST_DELETE = "artistDelete";
    public static final String ALBUM_DELETE = "albumDelete";
    public static final String TRACK_DELETE = "trackDelete";

    private String eventType;
    private Object payload;

}
