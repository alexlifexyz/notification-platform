package com.enterprise.notification.admin.dto.recipient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 收件人更新请求
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "收件人更新请求")
public class RecipientUpdateRequest {

    @NotBlank(message = "用户名称不能为空")
    @Schema(description = "用户名称", example = "张三 - 高级开发工程师", required = true)
    private String userName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138001")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@company.com")
    private String email;

    @Schema(description = "IM账号", example = "zhangsan")
    private String imAccount;

    @Schema(description = "偏好渠道列表", example = "[\"IN_APP\", \"EMAIL\", \"SMS\"]")
    private List<String> preferredChannels;

    @Schema(description = "所属组代码", example = "DEV_TEAM")
    private String groupCode;

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用", example = "true", required = true)
    private Boolean isEnabled;
}
