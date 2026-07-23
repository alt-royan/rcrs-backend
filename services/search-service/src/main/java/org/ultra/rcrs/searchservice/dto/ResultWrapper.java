package org.ultra.rcrs.searchservice.dto;

import org.ultra.rcrs.searchservice.enums.SearchType;

public interface ResultWrapper {

    SearchType getType();

    Object getData();
}
