package com.enterprise.notification.admin.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 站内信统计DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "站内信统计信息")
public class InAppMessageStatisticsDto {

    @Schema(description = "总消息数", example = "100")
    private Long totalCount;

    @Schema(description = "未读消息数", example = "25")
    private Long unreadCount;

    @Schema(description = "已读消息数", example = "75")
    private Long readCount;

    @Schema(description = "已读率", example = "75.0")
    private Double readRate;

    @Schema(description = "每日统计")
    private List<DailyStatistics> dailyStatistics;

    @Data
    @Schema(description = "每日统计")
    public static class DailyStatistics {

        @Schema(description = "日期", example = "2024-07-18")
        private String date;

        @Schema(description = "总消息数", example = "10")
        private Long totalCount;

        @Schema(description = "未读消息数", example = "3")
        private Long unreadCount;

        @Schema(description = "已读消息数", example = "7")
        private Long readCount;
    }
}
