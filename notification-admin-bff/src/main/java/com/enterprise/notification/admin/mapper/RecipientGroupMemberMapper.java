package com.enterprise.notification.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.notification.admin.entity.RecipientGroupMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收件人组成员Mapper接口
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Mapper
public interface RecipientGroupMemberMapper extends BaseMapper<RecipientGroupMember> {
}
