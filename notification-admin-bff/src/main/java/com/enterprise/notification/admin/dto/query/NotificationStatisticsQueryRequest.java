package com.enterprise.notification.admin.dto.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 通知统计查询请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class NotificationStatisticsQueryRequest {

    /**
     * 开始时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    /**
     * 分组方式：hour（按小时）、day（按天）
     */
    @Pattern(regexp = "^(hour|day)$", message = "分组方式只能是hour或day")
    private String groupBy = "day";

    /**
     * 渠道代码筛选
     */
    private String channelCode;

    /**
     * 模板代码筛选
     */
    private String templateCode;
}
