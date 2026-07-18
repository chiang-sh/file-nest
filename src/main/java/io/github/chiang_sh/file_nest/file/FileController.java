package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.file.dto.UpdateFileRequest;
import io.github.chiang_sh.file_nest.file.dto.UploadUrlRequest;
import io.github.chiang_sh.file_nest.file.dto.UploadUrlResponse;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionEntity;
import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.minio.Http;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File manipulation")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<UploadUrlResponse> uploadUrl(
            @AuthenticationPrincipal SecurityUser securityUser, @RequestBody UploadUrlRequest body)
            throws MinioException {
        FileEntity entity =
                fileService.createFile(
                        securityUser.getUsername(), body.filename(), body.folderUuid());
        String url = fileService.presignedUrl(entity.getStoragePath(), Http.Method.PUT, 5);
        UploadUrlResponse response = new UploadUrlResponse(entity.getUuid(), url);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{fileUuid}/confirm")
    public void confirmUpload(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID fileUuid) {
        try {
            fileService.confirmUpload(fileUuid);
        } catch (MinioException e) {
            throw new IllegalStateException("File " + fileUuid + " upload is not completed.");
        }
    }

    @GetMapping("/{fileUuid}")
    public String downloadUrl(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID fileUuid)
            throws MinioException {
        FilePermissionEntity permission =
                fileService.getAccessiblePermission(securityUser.getUsername(), fileUuid);
        FileEntity file = permission.getFile();
        return fileService.presignedUrl(file.getStoragePath(), Http.Method.GET);
    }

    @PatchMapping("/{fileUuid}")
    public FileResponse updateInfo(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID fileUuid,
            @RequestBody UpdateFileRequest body) {
        return fileService.updateInfo(
                securityUser.getUsername(), fileUuid, body.folderUuid(), body.filename());
    }
}
