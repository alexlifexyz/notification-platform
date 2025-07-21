package com.enterprise.notification.admin.dto.notification;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 通知记录查询请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class NotificationRecordQueryRequest {

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Long size = 10L;

    /**
     * 请求ID（模糊查询）
     */
    private String requestId;

    /**
     * 模板代码（模糊查询）
     */
    private String templateCode;

    /**
     * 接收者ID（模糊查询）
     */
    private String recipientId;

    /**
     * 发送状态
     */
    private String status;

    /**
     * 渠道代码
     */
    private String channelCode;

    /**
     * 服务商代码
     */
    private String providerCode;

    /**
     * 开始时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}
