package com.enterprise.notification.admin.dto.recipient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 收件人查询请求
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "收件人查询请求")
public class RecipientQueryRequest {

    @NotNull(message = "当前页不能为空")
    @Min(value = 1, message = "当前页必须大于0")
    @Schema(description = "当前页", example = "1", required = true)
    private Long current;

    @NotNull(message = "页大小不能为空")
    @Min(value = 1, message = "页大小必须大于0")
    @Schema(description = "页大小", example = "10", required = true)
    private Long size;

    @Schema(description = "用户ID（模糊查询）", example = "user")
    private String userId;

    @Schema(description = "用户名称（模糊查询）", example = "张")
    private String userName;

    @Schema(description = "手机号（模糊查询）", example = "138")
    private String phone;

    @Schema(description = "邮箱（模糊查询）", example = "@company.com")
    private String email;

    @Schema(description = "组代码", example = "DEV_TEAM")
    private String groupCode;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;
}
