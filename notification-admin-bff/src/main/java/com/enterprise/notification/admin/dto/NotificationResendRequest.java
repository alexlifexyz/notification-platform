package com.enterprise.notification.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 通知重发请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class NotificationResendRequest {

    /**
     * 重发原因
     */
    @NotBlank(message = "重发原因不能为空")
    private String reason;
}
