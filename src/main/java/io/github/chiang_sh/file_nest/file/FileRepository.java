package io.github.chiang_sh.file_nest.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.file.FileDto(f.uuid, f.name, f.contentType, f.size, f.createdAt, fp.permission)
            FROM FilePermissionEntity fp
            JOIN fp.file f
            WHERE f.folder IS NULL
            AND fp.user.id = :userId""")
    List<FileDto> findRootFiles(@Param("userId") Long userId);

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.file.FileDto(f.uuid, f.name, f.contentType, f.size, f.createdAt, fp.permission)
            FROM FilePermissionEntity fp
            JOIN fp.file f
            WHERE f.folder.uuid = :folderUuid
            AND fp.user.id = :userId""")
    List<FileDto> findChildrenFiles(@Param("userId") Long userId, @Param("folderUuid") UUID parentUuid);

    Optional<FileEntity> findByUuid(UUID uuid);
}
