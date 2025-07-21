package com.enterprise.notification.admin.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 渠道更新请求
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "渠道更新请求")
public class ChannelUpdateRequest {

    @NotBlank(message = "渠道名称不能为空")
    @Schema(description = "渠道名称", example = "微信企业通知", required = true)
    private String channelName;

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用", example = "true", required = true)
    private Boolean isEnabled;
}
