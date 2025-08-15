package com.enterprise.notification.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 直接发送通知请求DTO - 不使用模板配置
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DirectSendNotificationRequest extends BaseNotificationRequest {

    /**
     * 通知渠道代码列表：IN_APP, SMS, EMAIL, IM
     * 支持多个渠道同时发送
     */
    @NotEmpty(message = "通知渠道不能为空")
    private List<String> channelCodes;

    /**
     * 消息主题/标题
     */
    private String subject;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;



    /**
     * 验证渠道和联系方式的匹配性
     * 确保指定渠道的用户都有对应的联系方式
     */
    @AssertTrue(message = "用户联系方式与指定渠道不匹配")
    public boolean isValidChannelContacts() {
        return validateChannelContacts(channelCodes);
    }
}
