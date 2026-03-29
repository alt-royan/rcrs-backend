package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class ItemListDto<T> {

    private List<T> items;
    private Integer totalCount;

    public ItemListDto(Collection<T> items) {
        this.items = new ArrayList<>(items);
        this.totalCount = items.size();
    }
}
