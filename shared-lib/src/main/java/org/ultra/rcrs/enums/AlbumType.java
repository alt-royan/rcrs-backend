package org.ultra.rcrs.enums;

public enum AlbumType {
    FULL("album"), SINGLE("single"), EP("ep");

    private final String value;

    AlbumType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
