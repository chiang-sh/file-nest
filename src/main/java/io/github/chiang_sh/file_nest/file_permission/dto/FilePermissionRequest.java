package io.github.chiang_sh.file_nest.file_permission.dto;

import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;

public record FilePermissionRequest(String username, FilePermissionType permissionType) {}
