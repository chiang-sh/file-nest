package io.github.chiang_sh.file_nest.file_permission.dto;

import io.github.chiang_sh.file_nest.file_permission.FilePermissionEntity;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;

import java.util.UUID;

public record FilePermissionResponse(UUID uuid, String username, FilePermissionType type) {
    public static FilePermissionResponse from(FilePermissionEntity filePermission) {
        return new FilePermissionResponse(
                filePermission.getUuid(),
                filePermission.getUser().getUsername(),
                filePermission.getPermission());
    }
}
