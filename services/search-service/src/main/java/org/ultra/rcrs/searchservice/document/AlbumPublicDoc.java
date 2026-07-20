package org.ultra.rcrs.searchservice.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.ultra.rcrs.enums.EntityStatus;

import java.util.List;

@Data
@NoArgsConstructor
@Document(indexName = "albums-public", storeIdInSource = false)
public class AlbumPublicDoc implements AlbumDoc {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String year;

    @Field(type = FieldType.Keyword)
    private EntityStatus availability;

    @Field(type = FieldType.Nested)
    private List<NestedTrack> tracks;

    @Field(type = FieldType.Nested)
    private List<NestedArtist> artists;

}
