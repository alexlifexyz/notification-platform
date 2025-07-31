package com.enterprise.notification.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.notification.admin.entity.UserInAppMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户站内信Mapper
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Mapper
public interface UserInAppMessageMapper extends BaseMapper<UserInAppMessage> {

    /**
     * 获取站内信统计信息
     */
//    @Select({
//        "<script>",
//        "SELECT ",
//        "  COUNT(*) as totalCount,",
//        "  SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) as unreadCount,",
//        "  SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as readCount",
//        "FROM user_in_app_messages",
//        "WHERE 1=1",
//        "<if test='userId != null and userId != \"\"'>",
//        "  AND user_id = #{userId}",
//        "</if>",
//        "<if test='startTime != null'>",
//        "  AND created_at >= #{startTime}",
//        "</if>",
//        "<if test='endTime != null'>",
//        "  AND created_at <= #{endTime}",
//        "</if>",
//        "</script>"
//    })
    Map<String, Object> getStatistics(@Param("userId") String userId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 获取每日统计信息
     */
//    @Select({
//        "<script>",
//        "SELECT ",
//        "  DATE(created_at) as date,",
//        "  COUNT(*) as totalCount,",
//        "  SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) as unreadCount,",
//        "  SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as readCount",
//        "FROM user_in_app_messages",
//        "WHERE 1=1",
//        "<if test='userId != null and userId != \"\"'>",
//        "  AND user_id = #{userId}",
//        "</if>",
//        "<if test='startTime != null'>",
//        "  AND created_at >= #{startTime}",
//        "</if>",
//        "<if test='endTime != null'>",
//        "  AND created_at <= #{endTime}",
//        "</if>",
//        "GROUP BY DATE(created_at)",
//        "ORDER BY DATE(created_at)",
//        "</script>"
//    })
    List<Map<String, Object>> getDailyStatistics(@Param("userId") String userId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
}
