package com.enterprise.notification.admin.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内信DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "站内信信息")
public class InAppMessageDto {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联通知记录ID")
    private Long notificationId;

    @Schema(description = "用户ID", example = "user123")
    private String userId;

    @Schema(description = "消息主题", example = "您的订单已发货")
    private String subject;

    @Schema(description = "消息内容", example = "您的订单 ORD20240718001 已发货，快递单号：SF1234567890")
    private String content;

    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
