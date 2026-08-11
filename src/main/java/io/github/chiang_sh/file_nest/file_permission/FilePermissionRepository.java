package io.github.chiang_sh.file_nest.file_permission;

import io.github.chiang_sh.file_nest.file_permission.dto.FilePermissionResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilePermissionRepository extends JpaRepository<FilePermissionEntity, Long> {
    Optional<FilePermissionEntity> findByUserIdAndFileId(Long userId, Long fileId);

    Optional<FilePermissionEntity> findByUserIdAndFileUuidAndPermission(
            Long userId, UUID fileUuid, FilePermissionType permission);

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.file_permission.dto.FilePermissionResponse(p.uuid, p.user.username, p.permission)
            FROM FilePermissionEntity p
            WHERE p.file.uuid = :fileUuid""")
    List<FilePermissionResponse> findByFileUuid(UUID fileUuid);

    Optional<FilePermissionEntity> findByUuid(UUID uuid);
}
