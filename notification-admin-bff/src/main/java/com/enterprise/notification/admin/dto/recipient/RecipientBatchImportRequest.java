package com.enterprise.notification.admin.dto.recipient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 收件人批量导入请求
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "收件人批量导入请求")
public class RecipientBatchImportRequest {

    @NotBlank(message = "组代码不能为空")
    @Schema(description = "目标组代码", example = "DEV_TEAM", required = true)
    private String groupCode;

    @NotEmpty(message = "收件人列表不能为空")
    @Valid
    @Schema(description = "收件人列表", required = true)
    private List<RecipientImportItem> recipients;

    @Data
    @Schema(description = "收件人导入项")
    public static class RecipientImportItem {

        @NotBlank(message = "用户ID不能为空")
        @Schema(description = "用户ID", example = "user123", required = true)
        private String userId;

        @NotBlank(message = "用户名称不能为空")
        @Schema(description = "用户名称", example = "张三 - 开发工程师", required = true)
        private String userName;

        @Schema(description = "手机号", example = "13800138001")
        private String phone;

        @Schema(description = "邮箱", example = "zhangsan@company.com")
        private String email;

        @Schema(description = "IM账号", example = "zhangsan")
        private String imAccount;

        @Schema(description = "偏好渠道列表", example = "[\"IN_APP\", \"EMAIL\"]")
        private List<String> preferredChannels;
    }
}
