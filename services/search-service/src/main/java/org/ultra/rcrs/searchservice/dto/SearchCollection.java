package org.ultra.rcrs.searchservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchCollection<T extends ResultWrapper> {
    private int page;
    private int size;
    private long total;
    private String next;
    private List<T> items;

    public SearchCollection(int page, int size, long total, List<T> items) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }
}