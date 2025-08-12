package com.enterprise.notification.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.notification.enums.RecipientType;
import com.enterprise.notification.enums.SendStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知记录实体（发件箱/审计日志）
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notification")
public class Notification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 请求ID，用于幂等性
     */
    private String requestId;

    /**
     * 使用的模板代码
     */
    private String templateCode;

    /**
     * 发送渠道
     */
    private String channelCode;

    /**
     * 具体服务商代码
     */
    private String providerCode;

    /**
     * 接收者类型
     */
    private RecipientType recipientType;

    /**
     * 接收者ID（用户ID或组代码）
     */
    private String recipientId;

    /**
     * 接收者详细信息（JSON格式）
     */
    private String recipientInfo;

    /**
     * 模板参数（JSON格式）
     */
    private String templateParams;

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
    private SendStatus sendStatus;

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
