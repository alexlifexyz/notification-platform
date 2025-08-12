package com.enterprise.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知渠道实体
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notification_channel")
public class NotificationChannel {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 渠道代码：IN_APP, SMS, EMAIL, IM
     */
    private String channelCode;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
