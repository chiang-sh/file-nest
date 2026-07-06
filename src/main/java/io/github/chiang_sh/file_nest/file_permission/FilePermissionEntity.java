package io.github.chiang_sh.file_nest.file_permission;

import io.github.chiang_sh.file_nest.file.FileEntity;
import io.github.chiang_sh.file_nest.user.UserEntity;

import jakarta.persistence.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "file_permissions")
public class FilePermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FilePermissionType permission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public FileEntity getFile() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public FilePermissionType getPermission() {
        return permission;
    }

    public void setPermission(FilePermissionType permission) {
        this.permission = permission;
    }
}
