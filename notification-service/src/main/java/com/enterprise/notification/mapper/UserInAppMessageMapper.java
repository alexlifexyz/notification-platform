package com.enterprise.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.notification.entity.UserInAppMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户站内信Mapper接口
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Mapper
public interface UserInAppMessageMapper extends BaseMapper<UserInAppMessage> {
}
