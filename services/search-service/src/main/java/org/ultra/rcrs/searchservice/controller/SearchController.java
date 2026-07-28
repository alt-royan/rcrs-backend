package org.ultra.rcrs.searchservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.searchservice.dto.ErrorResponse;
import org.ultra.rcrs.searchservice.dto.SearchResponse;
import org.ultra.rcrs.searchservice.enums.SearchType;
import org.ultra.rcrs.searchservice.service.SearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Full-text search over the catalog (artists, albums, tracks) backed by Elasticsearch, with a public storefront mode and an unfiltered admin mode.")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(
            summary = "Search artists, albums and tracks",
            description = "Runs a text query against one or more entity types in parallel and returns a per-type page of results. "
                    + "In public mode (admin=false, the default) only entities that are publicly visible/published are matched; "
                    + "in admin mode (admin=true) the search is unfiltered and also matches non-public/unpublished or otherwise "
                    + "restricted-status documents. Passing several 'type' values (e.g. type=artist&type=album) searches across all "
                    + "of them at once, with each type's matches returned in its own section of the response, each paginated "
                    + "independently using the shared page/size parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully; matching results are returned per requested type.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "The request was malformed: a required parameter (e.g. 'q' or 'type') is missing, "
                    + "a parameter value could not be converted to its expected type (e.g. an invalid 'type' value), or validation otherwise failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed or is missing.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "The caller is authenticated but not allowed to perform this search (e.g. admin=true without the required role).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred while executing the search.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "The search backend (Elasticsearch) is currently unavailable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SearchResponse> search(
            HttpServletRequest request,
            @Parameter(description = "When true, runs the search unfiltered/admin-side, including non-public or unpublished-status documents. "
                    + "When false (default), only publicly-visible/published documents are matched.")
            @RequestParam(name = "admin", required = false, defaultValue = "false") boolean admin,
            @Parameter(description = "The free-text query to search for; matched against the searchable name/title fields of the requested entity types (e.g. artist name, album title, track title).", required = true)
            @RequestParam("q") String query,
            @Parameter(description = "One or more entity kinds to search: 'artist', 'album' and/or 'track'. Repeat the parameter to search several kinds at once (e.g. type=artist&type=track); each requested kind is searched in parallel and returned in its own section of the response.", required = true)
            @RequestParam(value = "type") SearchType[] types,
            @Parameter(description = "Zero-based page index applied independently to each requested type's result set. Defaults to 0.")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Maximum number of results per page, applied independently to each requested type's result set. Defaults to 20.")
            @RequestParam(value = "size", defaultValue = "20") int size) {
        SearchResponse searchResponse;
        if (admin) {
            searchResponse = searchService.searchAdmin(types, query, page, size, request);
        } else {
            searchResponse = searchService.searchPublic(types, query, page, size, request);
        }
        return ResponseEntity.ok(searchResponse);
    }
}
