package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file.dto.FileResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.file.dto.FileResponse(f.uuid, f.name, f.contentType, f.size, f.createdAt, fp.permission)
            FROM FilePermissionEntity fp
            JOIN fp.file f
            WHERE fp.folder IS NULL
            AND fp.user.id = :userId
            AND f.status = StatusType.COMPLETED
            ORDER BY f.createdAt
            LIMIT :pageSize OFFSET :offset""")
    List<FileResponse> findRootFiles(Long userId, int pageSize, int offset);

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.file.dto.FileResponse(f.uuid, f.name, f.contentType, f.size, f.createdAt, fp.permission)
            FROM FilePermissionEntity fp
            JOIN fp.file f
            WHERE fp.folder.uuid = :parentUuid
            AND fp.user.id = :userId
            AND f.status = StatusType.COMPLETED
            ORDER BY f.createdAt
            LIMIT :pageSize OFFSET :offset""")
    List<FileResponse> findChildrenFiles(Long userId, UUID parentUuid, int pageSize, int offset);

    Optional<FileEntity> findByUuid(UUID uuid);

    @Query(
            """
            SELECT f
            FROM FilePermissionEntity fp
            JOIN fp.file f
            WHERE fp.folder.id = :folderId
            AND fp.user.id = :userId
            AND f.status = StatusType.COMPLETED""")
    List<FileEntity> findByUserIdAndFolderId(
            @Param("userId") Long userId, @Param("folderId") Long folderId);

    int deleteByStatusAndCreatedAtBefore(StatusType status, OffsetDateTime datetime);
}
