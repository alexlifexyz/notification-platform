package com.enterprise.notification.async;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 异步任务管理器
 * 管理异步通知发送任务的状态和结果
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class AsyncTaskManager {

    /**
     * 任务状态缓存
     * key: requestId, value: AsyncTaskInfo
     */
    private final ConcurrentMap<String, AsyncTaskInfo> taskCache = new ConcurrentHashMap<>();

    /**
     * 注册异步任务
     *
     * @param requestId 请求ID
     * @param future    异步任务Future
     */
    public void registerTask(String requestId, CompletableFuture<Void> future) {
        AsyncTaskInfo taskInfo = new AsyncTaskInfo();
        taskInfo.setRequestId(requestId);
        taskInfo.setStatus(AsyncTaskStatus.RUNNING);
        taskInfo.setStartTime(LocalDateTime.now());
        taskInfo.setFuture(future);

        taskCache.put(requestId, taskInfo);
        
        log.info("注册异步任务: requestId={}", requestId);

        // 任务完成时更新状态
        future.whenComplete((result, throwable) -> {
            AsyncTaskInfo info = taskCache.get(requestId);
            if (info != null) {
                info.setEndTime(LocalDateTime.now());
                if (throwable != null) {
                    info.setStatus(AsyncTaskStatus.FAILED);
                    info.setErrorMessage(throwable.getMessage());
                    log.error("异步任务执行失败: requestId={}", requestId, throwable);
                } else {
                    info.setStatus(AsyncTaskStatus.COMPLETED);
                    log.info("异步任务执行完成: requestId={}", requestId);
                }
            }
        });
    }

    /**
     * 获取任务状态
     *
     * @param requestId 请求ID
     * @return 任务信息
     */
    public AsyncTaskInfo getTaskInfo(String requestId) {
        return taskCache.get(requestId);
    }

    /**
     * 取消任务
     *
     * @param requestId 请求ID
     * @return 是否成功取消
     */
    public boolean cancelTask(String requestId) {
        AsyncTaskInfo taskInfo = taskCache.get(requestId);
        if (taskInfo != null && taskInfo.getFuture() != null) {
            boolean cancelled = taskInfo.getFuture().cancel(true);
            if (cancelled) {
                taskInfo.setStatus(AsyncTaskStatus.CANCELLED);
                taskInfo.setEndTime(LocalDateTime.now());
                log.info("异步任务已取消: requestId={}", requestId);
            }
            return cancelled;
        }
        return false;
    }

    /**
     * 清理过期任务
     * 清理超过指定时间的已完成任务
     *
     * @param hours 保留小时数
     */
    public void cleanupExpiredTasks(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        taskCache.entrySet().removeIf(entry -> {
            AsyncTaskInfo info = entry.getValue();
            return info.getEndTime() != null && info.getEndTime().isBefore(cutoffTime);
        });
        
        log.info("清理过期异步任务完成，保留时间: {}小时", hours);
    }

    /**
     * 获取任务统计信息
     *
     * @return 任务统计
     */
    public TaskStatistics getStatistics() {
        TaskStatistics stats = new TaskStatistics();
        
        for (AsyncTaskInfo info : taskCache.values()) {
            stats.totalTasks++;
            switch (info.getStatus()) {
                case RUNNING:
                    stats.runningTasks++;
                    break;
                case COMPLETED:
                    stats.completedTasks++;
                    break;
                case FAILED:
                    stats.failedTasks++;
                    break;
                case CANCELLED:
                    stats.cancelledTasks++;
                    break;
            }
        }
        
        return stats;
    }

    /**
     * 异步任务信息
     */
    @Data
    public static class AsyncTaskInfo {
        private String requestId;
        private AsyncTaskStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String errorMessage;
        private CompletableFuture<Void> future;
    }

    /**
     * 异步任务状态
     */
    public enum AsyncTaskStatus {
        RUNNING,    // 运行中
        COMPLETED,  // 已完成
        FAILED,     // 失败
        CANCELLED   // 已取消
    }

    /**
     * 任务统计信息
     */
    @Data
    public static class TaskStatistics {
        private int totalTasks;
        private int runningTasks;
        private int completedTasks;
        private int failedTasks;
        private int cancelledTasks;
    }
}
