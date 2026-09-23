package io.github.chiang_sh.file_nest.file;

import io.minio.errors.MinioException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FileConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileConsumer.class);
    private final FileService fileService;

    public FileConsumer(FileService fileService) {
        this.fileService = fileService;
    }

    @KafkaListener(topics = "file-delete")
    public void consume(String uuidValue) throws MinioException {
        Optional<FileEntity> optional = fileService.findCleanupFile(uuidValue);
        if (optional.isEmpty()) {
            return;
        }
        FileEntity file = optional.get();
        if (file.getStatus() == StatusType.DELETING) {
            fileService.deleteObject(file);
            fileService.deleteRecord(file);
            LOGGER.info("Completed file cleanup: {}", uuidValue);
        }
    }
}
