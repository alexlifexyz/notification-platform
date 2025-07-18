package com.enterprise.notification.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知发送响应DTO - 共享模块
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class SendNotificationResponse {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 整体发送状态
     */
    private String status;

    /**
     * 发送结果详情
     */
    private List<SendResult> results;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 发送结果详情
     */
    @Data
    public static class SendResult {
        /**
         * 通知记录ID
         */
        private Long notificationId;

        /**
         * 渠道代码
         */
        private String channelCode;

        /**
         * 接收者ID
         */
        private String recipientId;

        /**
         * 发送状态
         */
        private String status;

        /**
         * 错误信息（如果发送失败）
         */
        private String errorMessage;

        /**
         * 发送时间
         */
        private LocalDateTime sentAt;
    }
}
