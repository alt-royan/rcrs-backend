package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"query", "page", "size", "total", "next", "items"})
@Schema(description = "A single page of results for one entity type.")
public class SearchCollection<T extends ResultWrapper> {
    @Schema(description = "The query text that produced this page of results, echoed back from the request.")
    private String query;
    @Schema(description = "The zero-based page index of this result set.")
    private int page;
    @Schema(description = "The maximum number of items requested per page.")
    private int size;
    @Schema(description = "The total number of matching documents for this entity type across all pages.")
    private long total;
    @Schema(description = "A cursor/URL for fetching the next page of results, or null if there are no further pages.")
    private String next;
    @Schema(description = "The matching items on this page, each wrapped with its entity type.")
    private List<T> items;

    public SearchCollection(String query, int page, int size, long total, List<T> items) {
        this.query = query;
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }
}