package com.enterprise.notification.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 通知发送请求DTO - 共享模块
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendNotificationRequest extends BaseNotificationRequest {

    /**
     * 模板代码
     */
    @NotBlank(message = "模板代码不能为空")
    private String templateCode;



    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;


}
