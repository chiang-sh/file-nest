package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.file.FileRepository;
import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.folder.dto.FileSystemDto;
import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;
import io.github.chiang_sh.file_nest.user.UserRepository;
import io.minio.errors.MinioException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class FolderService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Autowired
    public FolderService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    public List<FileSystemDto> getChildren(Long userId, int pageNumber, int pageSize) {
        return getChildren(userId, null, pageNumber, pageSize);
    }

    public List<FileSystemDto> getChildren(
            Long userId, UUID folderUuid, int pageNumber, int pageSize) {
        int folderCount = folderRepository.countByOwnerIdAndParentFolderUuid(userId, folderUuid);
        int totalFolderPageNumber = Math.ceilDiv(folderCount, pageSize);
        List<FolderResponse> folders = List.of();
        List<FileResponse> files = List.of();

        long rawOffset = ((long) pageNumber - 1) * pageSize;
        if (rawOffset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("PageNumber is too large.");
        }

        // Paginate folders first, then files.
        if (pageNumber < totalFolderPageNumber) {
            int offset = (int) rawOffset;
            folders =
                    folderUuid == null
                            ? folderRepository.findRootFolders(userId, pageSize, offset)
                            : folderRepository.findChildrenFolders(
                                    userId, folderUuid, pageSize, offset);
            ;
        } else if (pageNumber == totalFolderPageNumber) {
            int folderOffset = (int) rawOffset;
            folders =
                    folderUuid == null
                            ? folderRepository.findRootFolders(userId, pageSize, folderOffset)
                            : folderRepository.findChildrenFolders(
                                    userId, folderUuid, pageSize, folderOffset);
            int filePageSize = pageSize - folders.size();
            files =
                    folderUuid == null
                            ? fileRepository.findRootFiles(userId, filePageSize, 0)
                            : fileRepository.findChildrenFiles(userId, folderUuid, filePageSize, 0);
        } else {
            int offset = (int) rawOffset - folderCount;
            files =
                    folderUuid == null
                            ? fileRepository.findRootFiles(userId, pageSize, offset)
                            : fileRepository.findChildrenFiles(
                                    userId, folderUuid, pageSize, offset);
        }

        List<FileSystemDto> children = new ArrayList<>(folders.size() + files.size());
        children.addAll(folders);
        children.addAll(files);
        return children;
    }

    public FolderResponse create(Long userId, String name, UUID parentUuid) {
        FolderEntity newFolder = new FolderEntity();
        newFolder.setName(name);
        newFolder.setOwner(userRepository.getReferenceById(userId));

        if (parentUuid != null) {
            FolderEntity parent =
                    folderRepository
                            .findByUuidAndOwnerId(parentUuid, userId)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Parent folder not exist: " + parentUuid));
            newFolder.setParentFolder(parent);
        }

        newFolder = folderRepository.save(newFolder);
        return FolderResponse.from(newFolder);
    }

    public FolderResponse update(Long userId, UUID uuid, UUID parentUuid, String name) {
        FolderEntity folder =
                folderRepository
                        .findByUuidAndOwnerId(uuid, userId)
                        .orElseThrow(() -> new NoSuchElementException("Folder not exist: " + uuid));
        if (parentUuid != null) {
            List<UUID> parentAncestorUuids =
                    folderRepository.findParentFolderUuid(userId, parentUuid);
            // A cyclic relationship occurs when the current folder is an ancestor of the given
            // parent folder.
            if (parentAncestorUuids.contains(uuid)) {
                throw new IllegalArgumentException(
                        "Invalid parent folder: this operation would create a cyclic hierarchy.");
            }
            FolderEntity parent =
                    folderRepository
                            .findByUuidAndOwnerId(parentUuid, userId)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Folder not exist: " + parentUuid));
            folder.setParentFolder(parent);
        } else {
            folder.setParentFolder(null);
        }
        folder.setName(name);
        folderRepository.save(folder);
        return FolderResponse.from(folder);
    }

    public void delete(Long userId, UUID uuid) throws MinioException {
        FolderEntity folder =
                folderRepository
                        .findByUuidAndOwnerId(uuid, userId)
                        .orElseThrow(() -> new NoSuchElementException("Folder not exist: " + uuid));
        fileRepository.updateDeletingStatus(userId, folder.getId());
        folderRepository.delete(folder);
    }
}
