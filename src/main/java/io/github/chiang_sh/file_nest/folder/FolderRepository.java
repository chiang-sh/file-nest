package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.folder.dto.FolderResponse(f.uuid, f.name, f.createdAt)
            FROM FolderEntity f
            WHERE f.parentFolder IS NULL
            AND f.owner.id = :userId""")
    List<FolderResponse> findRootFolders(@Param("userId") Long userId);

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.folder.dto.FolderResponse(f.uuid, f.name, f.createdAt)
            FROM FolderEntity f
            WHERE f.parentFolder.uuid = :folderUuid
            AND f.owner.id = :userId""")
    List<FolderResponse> findChildrenFolders(
            @Param("userId") Long userId, @Param("folderUuid") UUID folderUuid);

    Optional<FolderEntity> findByUuid(UUID uuid);
}
