package com.enterprise.notification.task;

import com.enterprise.notification.async.AsyncTaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 异步任务清理定时任务
 * 定期清理过期的异步任务记录
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class AsyncTaskCleanupTask {

    @Autowired
    private AsyncTaskManager asyncTaskManager;

    @Value("${notification.async.cleanup.retention-hours:24}")
    private int retentionHours;

    /**
     * 清理过期任务
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredTasks() {
        log.info("开始清理过期异步任务，保留时间: {}小时", retentionHours);
        
        try {
            AsyncTaskManager.TaskStatistics beforeStats = asyncTaskManager.getStatistics();
            
            asyncTaskManager.cleanupExpiredTasks(retentionHours);
            
            AsyncTaskManager.TaskStatistics afterStats = asyncTaskManager.getStatistics();
            
            int cleanedCount = beforeStats.getTotalTasks() - afterStats.getTotalTasks();
            
            log.info("异步任务清理完成，清理数量: {}, 剩余任务: {}", cleanedCount, afterStats.getTotalTasks());
            
        } catch (Exception e) {
            log.error("异步任务清理失败", e);
        }
    }

    /**
     * 打印任务统计信息
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void printTaskStatistics() {
        try {
            AsyncTaskManager.TaskStatistics stats = asyncTaskManager.getStatistics();
            
            if (stats.getTotalTasks() > 0) {
                log.info("异步任务统计 - 总数: {}, 运行中: {}, 已完成: {}, 失败: {}, 已取消: {}", 
                        stats.getTotalTasks(),
                        stats.getRunningTasks(),
                        stats.getCompletedTasks(),
                        stats.getFailedTasks(),
                        stats.getCancelledTasks());
            }
            
        } catch (Exception e) {
            log.error("获取异步任务统计信息失败", e);
        }
    }
}
