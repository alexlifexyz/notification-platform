package com.enterprise.notification.admin.dto.group;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 收件人组查询请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class RecipientGroupQueryRequest {

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
     * 组代码（模糊查询）
     */
    private String groupCode;

    /**
     * 组名称（模糊查询）
     */
    private String groupName;

    /**
     * 是否启用
     */
    private Boolean isEnabled;
}
