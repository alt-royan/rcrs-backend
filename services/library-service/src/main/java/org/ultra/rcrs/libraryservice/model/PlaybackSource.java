package org.ultra.rcrs.libraryservice.model;

/**
 * Where a playback was started from. Captured at write time because it is free to
 * record then and impossible to reconstruct later, and it is what makes both the
 * "recently played" shelf and any future recommendation work possible.
 */
public enum PlaybackSource {
    ALBUM,
    PLAYLIST,
    ARTIST,
    SEARCH,
    LIBRARY,
    RADIO;

    /**
     * The library entity a play from this source should be attributed to in the
     * "recently played" shelf, or {@code null} when the source is not a browsable
     * entity and the track itself should be recorded instead.
     */
    public LibraryEntityType toEntityType() {
        return switch (this) {
            case ALBUM -> LibraryEntityType.ALBUM;
            case PLAYLIST -> LibraryEntityType.PLAYLIST;
            case ARTIST -> LibraryEntityType.ARTIST;
            case SEARCH, LIBRARY, RADIO -> null;
        };
    }
}
