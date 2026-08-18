package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.folder.dto.CreateFolderRequest;
import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;
import io.github.chiang_sh.file_nest.folder.dto.UpdateFolderRequest;
import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@Tag(name = "File manipulation")
public class FolderController {

    private static final String ROOT = "root";
    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping("/{folderUuid}/children")
    @Operation(description = "Use \"root\" as the folderUuid for the root folder.")
    public List<FileSystemDto> getChildren(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable String folderUuid,
            @RequestParam @Parameter(description = "Start from 1.") int pageNumber,
            @RequestParam int pageSize) {
        if (folderUuid == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (folderUuid.equals(ROOT)) {
            return folderService.getChildren(securityUser.getId(), pageNumber, pageSize);
        }
        return folderService.getChildren(
                securityUser.getId(), UUID.fromString(folderUuid), pageNumber, pageSize);
    }

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody CreateFolderRequest body) {
        if (body.name() == null || body.name().isEmpty()) {
            throw new IllegalArgumentException("The argument must not be null.");
        }
        FolderResponse response =
                folderService.create(securityUser.getId(), body.name(), body.parentUuid());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{folderUuid}")
    public FolderResponse updateFolder(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID folderUuid,
            @RequestBody UpdateFolderRequest body) {
        return folderService.update(
                securityUser.getId(), folderUuid, body.parentUuid(), body.name());
    }

    @DeleteMapping("/{folderUuid}")
    public ResponseEntity<Void> deleteFolder(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID folderUuid)
            throws MinioException {
        folderService.delete(securityUser.getId(), folderUuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
