package com.myopty.workflow.config;

import com.myopty.workflow.service.TodoTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueReminderScheduler {

    private final TodoTaskService todoTaskService;

    @PostConstruct
    public void init() {
        // Schedule to run every day at 9 AM
        // In production, would use: @Scheduled(cron = "0 0 9 * * *")
        // For now, using fixed delay for demonstration
    }

    @Scheduled(fixedDelayString = "86400000") // Run once every 24 hours (86400000 ms)
    public void checkAndSendOverdueReminders() {
        // This would typically require clientId, but for demo we'll log
        // In a real implementation, this would be triggered per client or via an event
        System.out.println("Checking for overdue tasks to send reminders...");
        // The actual reminder sending logic would use the shared module's NotificationSender
        // As per README: "a real transport replaces the log one by declaring its own bean"
    }
}