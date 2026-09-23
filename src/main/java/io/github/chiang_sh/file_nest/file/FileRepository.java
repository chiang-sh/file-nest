package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file.dto.FileResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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
            FROM FileEntity f
            WHERE f.status = :status
            AND f.id > :lastId
            AND (CAST(:datetime AS TIMESTAMP) IS NULL OR f.createdAt < :datetime)
            ORDER BY f.id ASC""")
    List<FileEntity> findCleanupBatch(
            StatusType status, Long lastId, Pageable pageable, OffsetDateTime datetime);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value =
                    """
            WITH RECURSIVE subtree(id) AS (
                SELECT id
                FROM folders
                WHERE id = :folderId
                AND owner_id = :userId
                UNION
                SELECT child.id
                FROM folders child
                JOIN subtree parent ON child.parent_folder_id = parent.id
                WHERE child.owner_id = :userId
            )
            UPDATE files f
            SET status = 'DELETING'
            FROM file_permissions fp
            JOIN subtree s ON fp.folder_id = s.id
            WHERE fp.file_id = f.id
                AND fp.user_id = :userId
                AND fp.permission IN ('OWNER', 'WRITE')
                AND f.status = 'COMPLETED';
            """,
            nativeQuery = true)
    int updateDeletingStatus(Long userId, Long folderId);
}
