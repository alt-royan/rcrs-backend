package org.ultra.rcrs.searchservice.dto;

import org.ultra.rcrs.searchservice.enums.SearchType;

import java.util.Map;

public interface ResultWrapper {

    SearchType getType();

    Map<String, Object> getData();
}
