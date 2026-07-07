package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File manipulation")
public class FileController {

    private FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/children")
    public List<FileDto> getChildren(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) UUID parentUuid) {
        return fileService.getChildren(securityUser.getUsername(), parentUuid);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam UUID parentUuid,
            @RequestPart MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            fileService.uploadFile(securityUser.getUsername(), parentUuid, file);
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully upload.");
        } catch (MinioException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
