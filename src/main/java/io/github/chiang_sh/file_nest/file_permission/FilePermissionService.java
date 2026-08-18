package io.github.chiang_sh.file_nest.file_permission;

import io.github.chiang_sh.file_nest.file.FileRepository;
import io.github.chiang_sh.file_nest.file.StatusType;
import io.github.chiang_sh.file_nest.file_permission.dto.FilePermissionResponse;
import io.github.chiang_sh.file_nest.user.UserRepository;
import io.minio.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class FilePermissionService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FilePermissionRepository filePermissionRepository;

    @Autowired
    public FilePermissionService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FilePermissionRepository filePermissionRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.filePermissionRepository = filePermissionRepository;
    }

    public List<FilePermissionResponse> getPermissions(Long ownerId, UUID fileUuid) {
        filePermissionRepository
                .findByUserIdAndFileUuidAndPermission(ownerId, fileUuid, FilePermissionType.OWNER)
                .orElseThrow(() -> new AccessDeniedException("Access denied: " + fileUuid));
        return filePermissionRepository.findByFileUuid(fileUuid);
    }

    public FilePermissionResponse create(
            Long ownerId, UUID fileUuid, String username, FilePermissionType permissionType) {
        filePermissionRepository
                .findByUserIdAndFileUuidAndPermission(ownerId, fileUuid, FilePermissionType.OWNER)
                .orElseThrow(() -> new AccessDeniedException("Access denied: " + fileUuid));
        if (filePermissionRepository.findByFileUuid(fileUuid).stream()
                .anyMatch(p -> p.username().equals(username))) {
            throw new IllegalArgumentException(
                    "Permission already exists for file " + fileUuid + " and user " + username);
        }
        if (permissionType.equals(FilePermissionType.OWNER)) {
            throw new IllegalArgumentException("Cannot assign OWNER permission.");
        }
        FilePermissionEntity newPermission = new FilePermissionEntity();
        newPermission.setUser(
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new AccessDeniedException("User not exist: " + username)));
        newPermission.setFile(
                fileRepository
                        .findByUuid(fileUuid)
                        .orElseThrow(
                                () -> new NoSuchElementException("File not exist: " + fileUuid)));
        newPermission.setPermission(permissionType);
        newPermission = filePermissionRepository.save(newPermission);
        return FilePermissionResponse.from(newPermission);
    }

    public FilePermissionResponse update(
            Long ownerId, UUID fileUuid, UUID permissionUuid, FilePermissionType permissionType) {
        FilePermissionEntity ownerPermission =
                filePermissionRepository
                        .findByUserIdAndFileUuidAndPermission(
                                ownerId, fileUuid, FilePermissionType.OWNER)
                        .orElseThrow(() -> new AccessDeniedException("Access denied: " + fileUuid));
        if (ownerPermission.getUuid().equals(permissionUuid)) {
            throw new IllegalArgumentException("Cannot change OWNER permission.");
        }
        if (permissionType.equals(FilePermissionType.OWNER)) {
            throw new IllegalArgumentException("Cannot assign OWNER permission.");
        }
        FilePermissionEntity permission =
                filePermissionRepository
                        .findByUuid(permissionUuid)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "Permission not exist: " + permissionUuid));
        permission.setPermission(permissionType);
        filePermissionRepository.save(permission);
        return FilePermissionResponse.from(permission);
    }

    public void delete(Long userId, UUID fileUuid, UUID permissionUuid) {
        FilePermissionEntity permission =
                filePermissionRepository
                        .findByUuid(permissionUuid)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "Permission not exist: " + permissionUuid));
        if (!permission.getUser().getId().equals(userId)) {
            filePermissionRepository
                    .findByUserIdAndFileUuidAndPermission(
                            userId, fileUuid, FilePermissionType.OWNER)
                    .orElseThrow(() -> new AccessDeniedException("Access denied: " + fileUuid));
        }
        if (permission.getPermission().equals(FilePermissionType.OWNER)) {
            throw new IllegalArgumentException("Owner cannot remove their own access permission.");
        }
        filePermissionRepository.delete(permission);
    }
}
