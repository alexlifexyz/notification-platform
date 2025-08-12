package com.enterprise.notification.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 收件人组实体
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("recipient_group")
public class RecipientGroup {

    @TableId(value = "id", type = IdType.AUTO)
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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
