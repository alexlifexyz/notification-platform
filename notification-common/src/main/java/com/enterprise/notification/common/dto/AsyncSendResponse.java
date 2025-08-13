package com.enterprise.notification.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步发送响应DTO
 * 用于异步发送接口的立即响应
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class AsyncSendResponse {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 异步任务状态
     */
    private String taskStatus;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 任务提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 状态查询URL
     */
    private String statusUrl;

    /**
     * 预计完成时间（可选）
     */
    private LocalDateTime estimatedCompletionTime;

    /**
     * 创建成功响应
     *
     * @param requestId 请求ID
     * @return 异步响应
     */
    public static AsyncSendResponse success(String requestId) {
        AsyncSendResponse response = new AsyncSendResponse();
        response.setRequestId(requestId);
        response.setTaskStatus("SUBMITTED");
        response.setMessage("通知发送任务已提交，正在异步处理中");
        response.setSubmittedAt(LocalDateTime.now());
        response.setStatusUrl("/api/v1/notifications/async/status/" + requestId);
        
        // 预计1分钟内完成
        response.setEstimatedCompletionTime(LocalDateTime.now().plusMinutes(1));
        
        return response;
    }

    /**
     * 创建失败响应
     *
     * @param requestId 请求ID
     * @param errorMessage 错误消息
     * @return 异步响应
     */
    public static AsyncSendResponse failure(String requestId, String errorMessage) {
        AsyncSendResponse response = new AsyncSendResponse();
        response.setRequestId(requestId);
        response.setTaskStatus("FAILED");
        response.setMessage("任务提交失败: " + errorMessage);
        response.setSubmittedAt(LocalDateTime.now());
        
        return response;
    }
}
