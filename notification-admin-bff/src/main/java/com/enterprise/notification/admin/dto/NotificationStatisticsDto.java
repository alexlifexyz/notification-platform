package com.enterprise.notification.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知统计DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class NotificationStatisticsDto {

    /**
     * 统计时间范围
     */
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * 总发送数量
     */
    private Long totalCount;

    /**
     * 成功数量
     */
    private Long successCount;

    /**
     * 失败数量
     */
    private Long failedCount;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 按渠道统计
     */
    private List<ChannelStatistics> channelStatistics;

    /**
     * 按模板统计
     */
    private List<TemplateStatistics> templateStatistics;

    /**
     * 按时间统计（时间序列）
     */
    private List<TimeSeriesStatistics> timeSeriesStatistics;

    @Data
    public static class ChannelStatistics {
        private String channelCode;
        private String channelName;
        private Long count;
        private Long successCount;
        private Long failedCount;
        private Double successRate;
    }

    @Data
    public static class TemplateStatistics {
        private String templateCode;
        private String templateName;
        private Long count;
        private Long successCount;
        private Long failedCount;
        private Double successRate;
    }

    @Data
    public static class TimeSeriesStatistics {
        private LocalDateTime time;
        private Long count;
        private Long successCount;
        private Long failedCount;
    }
}
