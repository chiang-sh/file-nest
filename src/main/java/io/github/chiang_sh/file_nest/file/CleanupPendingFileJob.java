package io.github.chiang_sh.file_nest.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CleanupPendingFileJob {

    private static final Logger logger = LoggerFactory.getLogger(CleanupPendingFileJob.class);
    private final FileService fileService;

    public CleanupPendingFileJob(FileService fileService) {
        this.fileService = fileService;
    }
    
    @Scheduled(cron = "0 0 3 * * *") // Run the job at 3:00 AM every day.
    public void cleanup() {
        logger.info("Cleanup pending files job started.");
        int deleteCount = fileService.deletePending();
        logger.info("Cleanup pending files job completed. Deleted {} files.", deleteCount);
    }
}
