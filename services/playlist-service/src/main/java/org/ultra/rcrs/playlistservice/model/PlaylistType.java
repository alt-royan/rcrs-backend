package org.ultra.rcrs.playlistservice.model;

/**
 * Playlists are user-created collections only.
 * <p>
 * {@code LIKED} and {@code DOWNLOADS} used to live here, but likes and downloads
 * are not playlists: they have no ordering, no track positions and no membership
 * semantics. They are owned by library-service, and keeping a second
 * representation of them here would have meant two sources of truth for the same
 * data. The enum is kept as a single value so the column has room for a future
 * kind such as {@code COLLABORATIVE}.
 */
public enum PlaylistType {
    CUSTOM
}
