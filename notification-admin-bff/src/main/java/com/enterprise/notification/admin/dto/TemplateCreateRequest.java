package com.enterprise.notification.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 模板创建请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class TemplateCreateRequest {

    /**
     * 模板代码
     */
    @NotBlank(message = "模板代码不能为空")
    private String templateCode;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 渠道代码
     */
    @NotBlank(message = "渠道代码不能为空")
    private String channelCode;

    /**
     * 主题模板
     */
    private String subject;

    /**
     * 内容模板
     */
    @NotBlank(message = "内容模板不能为空")
    private String content;

    /**
     * 第三方模板代码
     */
    private String thirdPartyTemplateCode;

    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
}
