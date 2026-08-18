package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.folder.dto.FolderResponse(f.uuid, f.name, f.createdAt)
            FROM FolderEntity f
            WHERE f.parentFolder IS NULL
            AND f.owner.id = :userId
            ORDER BY f.createdAt
            LIMIT :pageSize OFFSET :offset""")
    List<FolderResponse> findRootFolders(Long userId, int pageSize, int offset);

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.folder.dto.FolderResponse(f.uuid, f.name, f.createdAt)
            FROM FolderEntity f
            WHERE f.parentFolder.uuid = :folderUuid
            AND f.owner.id = :userId
            ORDER BY f.createdAt
            LIMIT :pageSize OFFSET :offset""")
    List<FolderResponse> findChildrenFolders(
            Long userId, UUID folderUuid, int pageSize, int offset);

    Optional<FolderEntity> findByUuidAndOwnerId(UUID uuid, Long ownerId);

    List<FolderEntity> findByParentFolderIdAndOwnerId(Long parentFolderId, Long ownerId);

    int countByOwnerId(Long ownerId);
}
