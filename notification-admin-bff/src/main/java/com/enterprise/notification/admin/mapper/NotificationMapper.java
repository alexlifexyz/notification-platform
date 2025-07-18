package com.enterprise.notification.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.notification.admin.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知记录Mapper接口
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 按渠道统计
     */
    List<Map<String, Object>> selectChannelStatistics(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 按模板统计
     */
    List<Map<String, Object>> selectTemplateStatistics(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 按小时统计
     */
    List<Map<String, Object>> selectHourlyStatistics(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 按天统计
     */
    List<Map<String, Object>> selectDailyStatistics(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 获取失败通知及重试次数
     */
    List<Map<String, Object>> selectFailedNotificationsWithRetryCount(@Param("startTime") LocalDateTime startTime,
                                                                      @Param("endTime") LocalDateTime endTime,
                                                                      @Param("limit") int limit);
}
