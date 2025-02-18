package com.kalaiselvan.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ApiService apiService = new ApiService();

    public void startScheduling() {
        scheduleNextApiCall(0); // Start immediately upon initialization
    }

    private void scheduleNextApiCall(long delaySeconds) {
        executor.schedule(() -> {
            try {
                ApiResponse response = apiService.makeApiCall();
                logger.info("API Call Successful. Duration: {}, TTL: {}", response.getDuration(), response.getTtl());
                scheduleNextApiCall(response.getTtl());
            } catch (Exception e) {
                logger.error("API Call Failed. Retrying in 60 seconds.", e);
                scheduleNextApiCall(60); // Retry after 60 seconds on failure
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}