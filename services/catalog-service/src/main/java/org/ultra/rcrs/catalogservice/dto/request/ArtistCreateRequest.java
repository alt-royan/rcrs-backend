package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;

import java.util.ArrayList;
import java.util.List;


@Data
public class ArtistCreateRequest {

    @NotNull
    private String name;

    //Ссылки на s3 приходят в формате s3://{bucket}/{key}
    @Pattern(regexp = "s3://[\\w\\-]+/[\\w\\-.]+", message = "URI must be s3://{bucket}/{key} formatted")
    private String avatarUri;

    @Valid
    private List<SocialLinkDto> socialLinks = new ArrayList<>();

}
