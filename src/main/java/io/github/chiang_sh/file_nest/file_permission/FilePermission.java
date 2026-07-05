package io.github.chiang_sh.file_nest.file_permission;

import io.github.chiang_sh.file_nest.file.File;
import io.github.chiang_sh.file_nest.user.User;

import jakarta.persistence.*;

import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "file_permissions")
public class FilePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FilePermissionType permission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FilePermissionType getPermission() {
        return permission;
    }

    public void setPermission(FilePermissionType permission) {
        this.permission = permission;
    }
}
