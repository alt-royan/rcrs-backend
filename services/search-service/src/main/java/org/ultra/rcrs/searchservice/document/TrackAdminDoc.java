package org.ultra.rcrs.searchservice.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.util.List;

@Data
@Document(indexName = "tracks-admin", storeIdInSource = false)
public class TrackAdminDoc implements TrackDoc {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Nested)
    private List<NestedArtist> artists;

    @Field(type = FieldType.Nested)
    private NestedAlbum album;

    @Field(type = FieldType.Keyword)
    private EntityStatus availability;

    @Field(type = FieldType.Keyword)
    private LifecycleStatus lifecycleStatus;

}