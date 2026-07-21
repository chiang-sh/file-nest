package io.github.chiang_sh.file_nest.file_permission;

import io.github.chiang_sh.file_nest.file.FileEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilePermissionRepository extends JpaRepository<FilePermissionEntity, Long> {
    Optional<FilePermissionEntity> findByUserIdAndFileId(Long userId, Long fileId);
}
