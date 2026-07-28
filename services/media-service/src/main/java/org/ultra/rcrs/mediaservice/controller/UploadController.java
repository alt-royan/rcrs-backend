package org.ultra.rcrs.mediaservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.mediaservice.dto.*;
import org.ultra.rcrs.mediaservice.service.AudioService;
import org.ultra.rcrs.mediaservice.service.ImageUploadService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media/upload")
@Tag(name = "Upload", description = "Client-driven upload flow: presigned S3 upload URLs for audio, upload status polling, and direct image uploads.")
public class UploadController {

    private final AudioService audioService;
    private final ImageUploadService imageUploadService;


    @Operation(
            summary = "Create a presigned S3 URL for uploading an audio file",
            description = "Registers a pending audio file upload of the given name and byte length, and returns the "
                    + "presigned S3 request (URL, HTTP method, required headers) and a uid the client uses to poll "
                    + "the upload status and later associate the file with a track."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned upload request returned successfully"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation (missing name or length)"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping(value = "/audio/pre-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S3PresignUrlResponse> getPreSignUrl(
            @Parameter(description = "File name and byte length of the audio file about to be uploaded")
            @RequestBody @Validated PreloadFileRequest request) {
        return ResponseEntity.ok(audioService.getPreSignUrl(request));
    }

    @Operation(
            summary = "Get processing status for previously uploaded audio files",
            description = "Returns the current file status (e.g. pending, processing, completed, failed) for each "
                    + "given upload uid, including a failure reason when applicable. Unknown/missing uids are omitted "
                    + "or reflected in the returned list depending on service behavior; a null/absent uids list is "
                    + "treated as empty."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status list returned successfully"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping(value = "/audio/get-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileStatusResponse>> getAudioStatus(
            @Parameter(description = "Upload uids previously returned by the pre-sign endpoint, to look up status for")
            @RequestParam(value = "uids") List<String> uids) {
        if (uids == null) {
            uids = new ArrayList<>();
        }
        return ResponseEntity.ok(audioService.getAudioStatus(uids));
    }

    @Operation(
            summary = "Upload an image directly",
            description = "Decodes and stores the given image payload (e.g. base64-encoded artwork/avatar), and "
                    + "returns the resulting storage URI."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded and URI returned successfully"),
            @ApiResponse(responseCode = "400", description = "Image payload is missing, malformed, or could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping(value = "/image")
    public ResponseEntity<ImageResponse> uploadImage(
            @Parameter(description = "Image upload payload containing the encoded image data")
            @RequestBody ImageUploadRequest request) {
        return ResponseEntity.ok(imageUploadService.uploadImage(request.getImage()));
    }
}
