package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.file.dto.UpdateFileRequest;
import io.github.chiang_sh.file_nest.file.dto.UploadUrlRequest;
import io.github.chiang_sh.file_nest.file.dto.UploadUrlResponse;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionEntity;
import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.minio.Http;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File manipulation")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
    private static final String FILE_DELETE_TOPIC = "file-delete";

    private final FileService fileService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public FileController(FileService fileService, KafkaTemplate<String, String> kafkaTemplate) {
        this.fileService = fileService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<UploadUrlResponse> uploadUrl(
            @AuthenticationPrincipal SecurityUser securityUser, @RequestBody UploadUrlRequest body)
            throws MinioException {
        FileEntity entity =
                fileService.createFile(securityUser.getId(), body.filename(), body.folderUuid());
        String url = fileService.presignedUrl(entity.getStoragePath(), Http.Method.PUT, 5);
        UploadUrlResponse response = new UploadUrlResponse(entity.getUuid(), url);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{fileUuid}/confirm")
    public void confirmUpload(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID fileUuid) {
        try {
            fileService.confirmUpload(securityUser.getId(), fileUuid);
        } catch (MinioException e) {
            throw new IllegalStateException("File " + fileUuid + " upload is not completed.");
        }
    }

    @GetMapping("/{fileUuid}")
    public String downloadUrl(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID fileUuid)
            throws MinioException {
        FilePermissionEntity permission =
                fileService.getAccessiblePermission(securityUser.getId(), fileUuid);
        FileEntity file = permission.getFile();
        return fileService.presignedUrl(file.getStoragePath(), Http.Method.GET);
    }

    @PutMapping("/{fileUuid}")
    @Operation(description = "All request fields are required.")
    public FileResponse updateInfo(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID fileUuid,
            @RequestBody UpdateFileRequest body) {
        if (body.filename() == null || body.filename().isBlank()) {
            throw new IllegalArgumentException("The argument must not be null.");
        }
        return fileService.updateInfo(
                securityUser.getId(), fileUuid, body.folderUuid(), body.filename());
    }

    @DeleteMapping("/{fileUuid}")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID fileUuid) {
        if (fileService.confirmDelete(securityUser.getId(), fileUuid)) {
            kafkaTemplate
                    .send(FILE_DELETE_TOPIC, fileUuid.toString())
                    .whenComplete(
                            (result, exception) -> {
                                if (exception != null) {
                                    LOGGER.error(
                                            "Failed to publish deletion for {}",
                                            fileUuid,
                                            exception);
                                }
                            });
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
