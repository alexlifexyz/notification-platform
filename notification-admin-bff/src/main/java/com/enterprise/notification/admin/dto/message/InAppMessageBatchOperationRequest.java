package com.enterprise.notification.admin.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 站内信批量操作请求
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Schema(description = "站内信批量操作请求")
public class InAppMessageBatchOperationRequest {

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型", example = "MARK_READ", required = true, 
            allowableValues = {"MARK_READ", "MARK_UNREAD", "DELETE"})
    private OperationType operation;

    @NotEmpty(message = "消息ID列表不能为空")
    @Schema(description = "消息ID列表", example = "[1, 2, 3, 4, 5]", required = true)
    private List<Long> messageIds;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        /**
         * 标记已读
         */
        MARK_READ,
        
        /**
         * 标记未读
         */
        MARK_UNREAD,
        
        /**
         * 删除
         */
        DELETE
    }
}
