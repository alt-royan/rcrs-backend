package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"query", "page", "size", "total", "next", "items"})
public class SearchCollection<T extends ResultWrapper> {
    private String query;
    private int page;
    private int size;
    private long total;
    private String next;
    private List<T> items;

    public SearchCollection(String query, int page, int size, long total, List<T> items) {
        this.query = query;
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }
}