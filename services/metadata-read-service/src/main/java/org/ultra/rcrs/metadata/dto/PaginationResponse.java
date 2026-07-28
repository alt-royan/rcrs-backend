package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Generic paginated response envelope wrapping a page of items along with paging metadata.")
public record PaginationResponse<T>(
        @Schema(description = "Items contained in the current page.") List<T> items,
        @Schema(description = "Total number of items matching the query, across all pages.") long totalCount,
        @Schema(description = "Zero-based offset of the first item in this page, relative to the full result set.") int offset,
        @Schema(description = "Maximum number of items requested for this page.") int limit) {
}
