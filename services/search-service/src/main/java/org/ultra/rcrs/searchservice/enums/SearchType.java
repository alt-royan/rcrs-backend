package org.ultra.rcrs.searchservice.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum SearchType {

    @JsonProperty("artist")
    ARTIST("artist"),

    @JsonProperty("album")
    ALBUM("album"),

    @JsonProperty("track")
    TRACK("track");

    @Getter
    @JsonValue
    private final String value;

    SearchType(String value) {
        this.value = value;
    }

}