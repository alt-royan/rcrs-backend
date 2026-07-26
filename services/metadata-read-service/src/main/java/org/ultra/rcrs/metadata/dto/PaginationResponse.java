package org.ultra.rcrs.metadata.dto;

import java.util.List;

public record PaginationResponse<T>(List<T> items, long totalCount, int offset, int limit) {
}
