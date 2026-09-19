package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionEntity;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionRepository;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;
import io.github.chiang_sh.file_nest.folder.FolderEntity;
import io.github.chiang_sh.file_nest.folder.FolderRepository;
import io.github.chiang_sh.file_nest.minio.MinioProperties;
import io.github.chiang_sh.file_nest.user.UserRepository;
import io.minio.*;
import io.minio.errors.MinioException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FilePermissionRepository filePermissionRepository;
    private final FolderRepository folderRepository;
    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Autowired
    public FileService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FilePermissionRepository filePermissionRepository,
            FolderRepository folderRepository,
            MinioClient minioClient,
            MinioProperties properties) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.filePermissionRepository = filePermissionRepository;
        this.folderRepository = folderRepository;
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public FileEntity createFile(Long userId, String originalFilename, UUID parentUuid) {
        UUID uuid = UUID.randomUUID();
        String filename = FilenameUtils.getName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        String minioFilename = uuid + "." + extension;
        String objectKey = String.join("/", "users", Long.toString(userId), minioFilename);

        FileEntity file = new FileEntity();
        file.setUuid(uuid);
        file.setName(filename);
        file.setStoragePath(objectKey);
        file.setStatus(StatusType.PENDING);
        fileRepository.save(file);

        FilePermissionEntity permission = new FilePermissionEntity();
        permission.setUser(userRepository.getReferenceById(userId));
        permission.setFile(file);
        permission.setPermission(FilePermissionType.OWNER);
        if (parentUuid != null) {
            FolderEntity folder =
                    folderRepository
                            .findByUuidAndOwnerId(parentUuid, userId)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Folder not exist: " + parentUuid));
            permission.setFolder(folder);
        }
        filePermissionRepository.save(permission);

        return file;
    }

    public String presignedUrl(String objectKey, Http.Method method) throws MinioException {
        return presignedUrl(objectKey, method, 10);
    }

    public String presignedUrl(String objectKey, Http.Method method, int expiredMinutes)
            throws MinioException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(method)
                        .bucket(properties.getBucketName())
                        .object(objectKey)
                        .expiry(expiredMinutes, TimeUnit.MINUTES)
                        .build());
    }

    public void confirmUpload(Long userId, UUID uuid) throws MinioException {
        FilePermissionEntity permission = getAccessiblePermission(userId, uuid, StatusType.PENDING);
        FileEntity file = permission.getFile();

        StatObjectResponse stat =
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(properties.getBucketName())
                                .object(file.getStoragePath())
                                .build());
        file.setContentType(stat.contentType());
        file.setSize(stat.size());
        file.setStatus(StatusType.COMPLETED);
        fileRepository.save(file);
    }

    public FilePermissionEntity getAccessiblePermission(Long userId, UUID uuid) {
        return getAccessiblePermission(userId, uuid, StatusType.COMPLETED);
    }

    public FilePermissionEntity getAccessiblePermission(
            Long userId, UUID uuid, StatusType statusType) {
        FileEntity file =
                fileRepository
                        .findByUuid(uuid)
                        .orElseThrow(() -> new NoSuchElementException("File not exist: " + uuid));
        if (!file.getStatus().equals(statusType)) {
            throw new IllegalStateException(
                    "File "
                            + uuid
                            + " upload status is "
                            + file.getStatus().name()
                            + ", not "
                            + statusType.name()
                            + ".");
        }
        return filePermissionRepository
                .findByUserIdAndFileId(userId, file.getId())
                .orElseThrow(() -> new AccessDeniedException("Access denied: " + uuid));
    }

    public FileResponse updateInfo(Long userId, UUID uuid, UUID folderUuid, String filename) {
        FilePermissionEntity permission = getAccessiblePermission(userId, uuid);
        FileEntity file = permission.getFile();
        if (folderUuid != null) {
            FolderEntity folder =
                    folderRepository
                            .findByUuidAndOwnerId(folderUuid, userId)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Folder not exist: " + folderUuid));
            permission.setFolder(folder);
        } else {
            permission.setFolder(null);
        }
        if (permission.getPermission() != FilePermissionType.OWNER
                && permission.getPermission() != FilePermissionType.WRITE
                && !filename.equals(file.getName())) {
            throw new AccessDeniedException(
                    "Renaming a file requires OWNER or WRITE permission.: " + uuid);
        }
        file.setName(filename);
        fileRepository.save(file);
        return FileResponse.from(file, permission);
    }

    public void confirmDelete(Long userId, UUID uuid) {
        FilePermissionEntity permission = getAccessiblePermission(userId, uuid);
        // Delete the file record if the permission is OWNER and WRITE.
        if (permission.getPermission() == FilePermissionType.OWNER
                || permission.getPermission() == FilePermissionType.WRITE) {
            FileEntity file = permission.getFile();
            file.setStatus(StatusType.DELETING);
            fileRepository.save(file);
        }
        filePermissionRepository.delete(permission);
    }

    public List<FileEntity> findCleanupFiles(
            StatusType status, Long lastId, OffsetDateTime datetime) {
        return fileRepository.findCleanupBatch(status, lastId, PageRequest.of(0, 100), datetime);
    }

    public void deleteObject(FileEntity file) throws MinioException {
        String storagePath = file.getStoragePath();
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(storagePath)
                        .build());
    }

    public void deleteRecord(FileEntity file) {
        fileRepository.delete(file);
    }
}
