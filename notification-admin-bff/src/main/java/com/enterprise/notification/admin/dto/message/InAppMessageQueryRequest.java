package com.enterprise.notification.admin.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 站内信查询请求
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "站内信查询请求")
public class InAppMessageQueryRequest {

    @NotNull(message = "当前页不能为空")
    @Min(value = 1, message = "当前页必须大于0")
    @Schema(description = "当前页", example = "1", required = true)
    private Long current;

    @NotNull(message = "页大小不能为空")
    @Min(value = 1, message = "页大小必须大于0")
    @Schema(description = "页大小", example = "20", required = true)
    private Long size;

    @Schema(description = "用户ID", example = "user123")
    private String userId;

    @Schema(description = "消息主题（模糊查询）", example = "订单")
    private String subject;

    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间", example = "2024-07-18 00:00:00")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间", example = "2024-07-18 23:59:59")
    private LocalDateTime endTime;
}
