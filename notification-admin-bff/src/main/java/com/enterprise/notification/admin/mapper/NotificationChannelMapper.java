package com.enterprise.notification.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.notification.admin.entity.NotificationChannel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知渠道Mapper接口
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Mapper
public interface NotificationChannelMapper extends BaseMapper<NotificationChannel> {
}
