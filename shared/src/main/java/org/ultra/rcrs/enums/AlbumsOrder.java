package org.ultra.rcrs.enums;

public enum AlbumsOrder {
    ASC("asc"), DESC("desc");

    private final String value;

    AlbumsOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
