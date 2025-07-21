package com.enterprise.notification.admin.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 渠道DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "通知渠道信息")
public class ChannelDto {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "渠道代码", example = "IN_APP")
    private String channelCode;

    @Schema(description = "渠道名称", example = "站内信")
    private String channelName;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
