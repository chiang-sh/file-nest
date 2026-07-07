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
            SELECT NEW io.github.chiang_sh.file_nest.file.FileDto(f.uuid, f.name, f.type, f.size, f.createdAt, fp.permission)
            FROM FilePermissionEntity fp
            JOIN fp.file f
            WHERE f.parent IS NULL
            AND fp.user.id = :userId""")
    List<FileDto> findRootChildren(@Param("userId") Long userId);

    @Query(
            """
            SELECT NEW io.github.chiang_sh.file_nest.file.FileDto(f.uuid, f.name, f.type, f.size, f.createdAt, fp.permission)
            FROM FilePermissionEntity fp
            JOIN fp.file f
            WHERE f.parent.uuid = :parentUuid
            AND fp.user.id = :userId""")
    List<FileDto> findChildren(@Param("userId") Long userId, @Param("parentUuid") UUID parentUuid);

    Optional<FileEntity> findByUuid(UUID uuid);
}
