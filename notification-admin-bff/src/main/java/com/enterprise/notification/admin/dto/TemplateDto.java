package com.enterprise.notification.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class TemplateDto {

    /**
     * 模板ID
     */
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
     * 渠道代码
     */
    private String channelCode;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 主题模板
     */
    private String subject;

    /**
     * 内容模板
     */
    private String content;

    /**
     * 第三方模板代码
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
