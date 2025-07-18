package com.enterprise.notification.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收件人组成员DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class RecipientGroupMemberDto {

    /**
     * 成员ID
     */
    private Long id;

    /**
     * 组代码
     */
    private String groupCode;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * IM账号
     */
    private String imAccount;

    /**
     * 偏好渠道列表
     */
    private List<String> preferredChannels;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
