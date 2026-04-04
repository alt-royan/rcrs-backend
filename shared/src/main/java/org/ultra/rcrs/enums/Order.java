package org.ultra.rcrs.enums;

public enum Order {
    ASC("asc"), DESC("desc");

    private final String value;

    Order(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
