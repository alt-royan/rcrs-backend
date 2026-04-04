package org.ultra.rcrs.enums;

public enum ArtistRole {
    MAIN_ARTIST("main_artist"), FEATURED_ARTIST("appears_on");

    private final String value;

    ArtistRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
