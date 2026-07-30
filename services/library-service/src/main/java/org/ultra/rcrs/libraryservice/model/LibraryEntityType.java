package org.ultra.rcrs.libraryservice.model;

import org.ultra.rcrs.exceptions.BadRequestException;

import java.util.Locale;

/**
 * Kinds of entity a user can hold in their library.
 * <p>
 * Deliberately local rather than widening shared-lib's {@code EntityType}, which is
 * {@code {ALBUM, TRACK, ARTIST}} and is switched on exhaustively by search-service and
 * metadata-read-service; adding {@code PLAYLIST} there would mean auditing every one
 * of those switches for a concept those services have no notion of.
 */
public enum LibraryEntityType {

    TRACK("tracks"),
    ALBUM("albums"),
    ARTIST("artists"),
    PLAYLIST("playlists");

    private final String pathSegment;

    LibraryEntityType(String pathSegment) {
        this.pathSegment = pathSegment;
    }

    public String getPathSegment() {
        return pathSegment;
    }

    /**
     * Resolves the plural URL segment used in {@code /me/library/likes/{type}} paths.
     */
    public static LibraryEntityType fromPathSegment(String segment) {
        for (LibraryEntityType type : values()) {
            if (type.pathSegment.equalsIgnoreCase(segment)) {
                return type;
            }
        }
        throw new BadRequestException("unknown library entity type: " + segment);
    }

    /**
     * Collections are the only things that can be downloaded as a unit; a single
     * track is downloaded through the track endpoints instead.
     */
    public boolean isDownloadableCollection() {
        return this == ALBUM || this == PLAYLIST;
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
