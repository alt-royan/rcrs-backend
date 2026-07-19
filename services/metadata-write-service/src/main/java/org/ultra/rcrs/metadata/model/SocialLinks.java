package org.ultra.rcrs.metadata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.metadata.dto.SocialLinkDto;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SocialLinks {

    private List<Link> items;

    public SocialLinks(List<SocialLinkDto> dtos) {
        if (dtos == null) {
            this.items = new ArrayList<>();
        }else {
            this.items = dtos.stream().map(dto -> new Link(dto.getResourceName(), dto.getUrl().toString())).toList();
        }
    }

    public SocialLinks(SocialLinkDto dto) {
        this.items = List.of(new Link(dto.getResourceName(), dto.getUrl().toString()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Link {

        @JsonProperty("resource_name")
        private String resourceName;

        @JsonProperty("url")
        private String url;
    }

}
