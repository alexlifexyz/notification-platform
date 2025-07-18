package com.enterprise.notification.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知记录DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class NotificationRecordDto {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 模板代码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 渠道代码
     */
    private String channelCode;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 服务商代码
     */
    private String providerCode;

    /**
     * 接收者类型
     */
    private String recipientType;

    /**
     * 接收者ID
     */
    private String recipientId;

    /**
     * 接收者信息
     */
    private Map<String, Object> recipientInfo;

    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 渲染后的主题
     */
    private String renderedSubject;

    /**
     * 渲染后的内容
     */
    private String renderedContent;

    /**
     * 发送状态
     */
    private String sendStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 发送时间
     */
    private LocalDateTime sentAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
