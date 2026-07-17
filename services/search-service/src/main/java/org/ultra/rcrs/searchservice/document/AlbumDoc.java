package org.ultra.rcrs.searchservice.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "albums", storeIdInSource = false)
public class AlbumDoc {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Integer)
    private Integer year;

    @Field(type = FieldType.Keyword)
    private Boolean published;

    @Field(type = FieldType.Nested)
    private List<NestedTrack> tracks;

    @Field(type = FieldType.Nested)
    private List<NestedArtist> artists;

}
