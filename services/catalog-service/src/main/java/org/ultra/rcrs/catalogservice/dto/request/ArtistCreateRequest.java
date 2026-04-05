package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.model.SocialLink;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class ArtistCreateRequest {

    @NotNull
    private String name;

    private List<SocialLink> socialLinks;

    //Ссылки на s3 приходят в формате s3://{bucket}/{key}
    @Pattern(regexp = "s3://[\\w\\-]+/[\\w\\-.]+", message = "URI must be s3://{bucket}/{key} formatted")
    private String avatarUri;

}