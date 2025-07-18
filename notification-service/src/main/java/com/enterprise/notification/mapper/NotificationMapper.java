package com.enterprise.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知记录Mapper接口
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
