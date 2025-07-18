package com.enterprise.notification.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 收件人组创建请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class RecipientGroupCreateRequest {

    /**
     * 组代码
     */
    @NotBlank(message = "组代码不能为空")
    private String groupCode;

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    private String groupName;

    /**
     * 组描述
     */
    private String description;

    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
}
