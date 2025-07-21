package com.enterprise.notification.admin.dto.group;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收件人组DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class RecipientGroupDto {

    /**
     * 组ID
     */
    private Long id;

    /**
     * 组代码
     */
    private String groupCode;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 组描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
