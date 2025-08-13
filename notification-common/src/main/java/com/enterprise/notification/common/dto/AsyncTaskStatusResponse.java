package com.enterprise.notification.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步任务状态响应DTO
 * 用于查询异步任务的执行状态和结果
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class AsyncTaskStatusResponse {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 任务状态：RUNNING, COMPLETED, FAILED, CANCELLED
     */
    private String taskStatus;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

    /**
     * 错误消息（失败时）
     */
    private String errorMessage;

    /**
     * 任务结果（完成时）
     */
    private SendNotificationResponse result;

    /**
     * 进度信息（可选）
     */
    private String progressInfo;

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(taskStatus) || "FAILED".equals(taskStatus) || "CANCELLED".equals(taskStatus);
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "COMPLETED".equals(taskStatus);
    }

    /**
     * 获取执行时长（毫秒）
     */
    public Long getExecutionTimeMs() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return null;
    }
}
