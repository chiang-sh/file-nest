package io.github.chiang_sh.file_nest.file_permission;

import io.github.chiang_sh.file_nest.file_permission.dto.FilePermissionRequest;
import io.github.chiang_sh.file_nest.file_permission.dto.FilePermissionResponse;
import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files/{fileUuid}/permissions")
@Tag(name = "File permission manipulation")
public class FilePermissionController {

    private final FilePermissionService filePermissionService;

    @Autowired
    public FilePermissionController(FilePermissionService filePermissionService) {
        this.filePermissionService = filePermissionService;
    }

    @GetMapping
    public List<FilePermissionResponse> getPermissions(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID fileUuid) {
        return filePermissionService.getPermissions(securityUser.getId(), fileUuid);
    }

    @PostMapping
    public FilePermissionResponse createPermission(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID fileUuid,
            @RequestBody FilePermissionRequest body) {
        return filePermissionService.create(
                securityUser.getId(), fileUuid, body.username(), body.permissionType());
    }

    @PatchMapping("/{permissionUuid}")
    public FilePermissionResponse updatePermission(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID fileUuid,
            @PathVariable UUID permissionUuid,
            @RequestBody FilePermissionRequest body) {
        return filePermissionService.update(
                securityUser.getId(), fileUuid, permissionUuid, body.permissionType());
    }

    @DeleteMapping("/{permissionUuid}")
    public ResponseEntity<Void> deletePermission(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID fileUuid,
            @PathVariable UUID permissionUuid) {
        filePermissionService.delete(securityUser.getId(), fileUuid, permissionUuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
