package org.ultra.rcrs.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogCdcEvent {

    public static final String UPSERT = "UPSERT";
    public static final String DELETE = "DELETE";

    public static final String ARTIST = "ARTIST";
    public static final String ALBUM = "ALBUM";
    public static final String TRACK = "TRACK";

    private String action;
    private String entityType;
    private Object payload;
}
