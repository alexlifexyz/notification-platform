package com.enterprise.notification.admin.dto.recipient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收件人DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "收件人信息")
public class RecipientDto {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID", example = "user123")
    private String userId;

    @Schema(description = "用户名称", example = "张三 - 开发工程师")
    private String userName;

    @Schema(description = "手机号", example = "13800138001")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@company.com")
    private String email;

    @Schema(description = "IM账号", example = "zhangsan")
    private String imAccount;

    @Schema(description = "偏好渠道列表", example = "[\"IN_APP\", \"EMAIL\", \"SMS\"]")
    private List<String> preferredChannels;

    @Schema(description = "所属组代码", example = "DEV_TEAM")
    private String groupCode;

    @Schema(description = "所属组名称", example = "开发团队")
    private String groupName;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
