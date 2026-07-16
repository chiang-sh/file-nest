package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.file.FileRepository;
import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;
import io.github.chiang_sh.file_nest.user.UserEntity;
import io.github.chiang_sh.file_nest.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public List<FileSystemDto> getChildren(String username) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        List<FolderResponse> folders = folderRepository.findRootFolders(user.getId());
        List<FileResponse> files = fileRepository.findRootFiles(user.getId());
        List<FileSystemDto> children = new ArrayList<>(folders.size() + files.size());
        children.addAll(folders);
        children.addAll(files);
        return children;
    }

    public List<FileSystemDto> getChildren(String username, UUID folderUuid) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        List<FolderResponse> folders = folderRepository.findChildrenFolders(user.getId(), folderUuid);
        List<FileResponse> files = fileRepository.findChildrenFiles(user.getId(), folderUuid);
        List<FileSystemDto> children = new ArrayList<>(folders.size() + files.size());
        children.addAll(folders);
        children.addAll(files);
        return children;
    }

    public FolderResponse create(String username, String name) {
        return create(username, name, null);
    }

        UserEntity user = userRepository.findByUsername(username).orElseThrow();
    public FolderResponse create(String username, String name, UUID parentUuid) {
        FolderEntity newFolder = new FolderEntity();
        newFolder.setName(name);
        newFolder.setOwner(user);

        if (parentUuid != null) {
            FolderEntity parent = folderRepository.findByUuid(parentUuid).orElseThrow(() -> new NoSuchElementException("Parent folder not exist."));
            newFolder.setParentFolder(parent);
        }

        newFolder = folderRepository.save(newFolder);
        return FolderResponse.from(newFolder);
    }
}
