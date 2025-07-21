package com.enterprise.notification.admin.dto.group;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * 组成员查询请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class GroupMemberQueryRequest {

    /**
     * 组代码
     */
    @NotBlank(message = "组代码不能为空")
    private String groupCode;

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
     * 用户ID（模糊查询）
     */
    private String userId;

    /**
     * 用户名称（模糊查询）
     */
    private String userName;

    /**
     * 是否启用
     */
    private Boolean isEnabled;
}
