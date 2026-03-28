package org.ultra.rcrs.enums;

public enum AlbumGroup {
    ALBUM("album"), SINGLE("single"), EP("ep"), APPEARS_ON("appears_on");

    private final String value;

    AlbumGroup(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
