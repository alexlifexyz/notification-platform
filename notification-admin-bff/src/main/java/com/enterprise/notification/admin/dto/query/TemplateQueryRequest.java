package com.enterprise.notification.admin.dto.query;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 模板查询请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class TemplateQueryRequest {

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
     * 模板代码（模糊查询）
     */
    private String templateCode;

    /**
     * 模板名称（模糊查询）
     */
    private String templateName;

    /**
     * 渠道代码
     */
    private String channelCode;

    /**
     * 是否启用
     */
    private Boolean isEnabled;
}
