package com.enterprise.notification.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知模板实体
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notification_templates")
public class NotificationTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板代码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 关联渠道代码
     */
    private String channelCode;

    /**
     * 主题/标题模板
     */
    private String subject;

    /**
     * 内容模板，支持${variable}占位符
     */
    private String content;

    /**
     * 第三方服务商模板代码（如短信模板ID）
     */
    private String thirdPartyTemplateCode;

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
