package com.enterprise.notification.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 收件人组成员更新请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class RecipientGroupMemberUpdateRequest {

    /**
     * 用户名称
     */
    @NotBlank(message = "用户名称不能为空")
    private String userName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * IM账号
     */
    private String imAccount;

    /**
     * 偏好渠道列表
     */
    private List<String> preferredChannels;

    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
}
