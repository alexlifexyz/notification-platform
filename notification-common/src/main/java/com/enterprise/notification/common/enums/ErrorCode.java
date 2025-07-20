package com.enterprise.notification.common.enums;

/**
 * 错误码枚举
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
public enum ErrorCode {

    // 通用错误码 (1000-1999)
    SUCCESS("1000", "success"),
    UNKNOWN_ERROR("1001", "error.unknown"),
    INVALID_PARAMETER("1002", "error.invalid.parameter"),
    NETWORK_ERROR("1003", "error.network"),
    SERVER_ERROR("1004", "error.server"),

    // 通知服务错误码 (2000-2999)
    TEMPLATE_NOT_FOUND("2001", "error.template.not.found"),
    TEMPLATE_CODE_EXISTS("2002", "error.template.code.exists"),
    TEMPLATE_INVALID("2003", "error.template.invalid"),
    CHANNEL_NOT_FOUND("2004", "error.channel.not.found"),
    CHANNEL_INVALID("2005", "error.channel.invalid"),
    RECIPIENT_NOT_FOUND("2006", "error.recipient.not.found"),
    RECIPIENT_INVALID("2007", "error.recipient.invalid"),
    REQUEST_ALREADY_PROCESSED("2008", "error.request.already.processed"),
    SEND_FAILED("2009", "error.send.failed"),

    // 模板相关错误码 (2100-2199)
    TEMPLATE_RENDER_FAILED("2101", "error.template.render.failed"),
    TEMPLATE_VARIABLE_MISSING("2102", "error.template.variable.missing"),
    TEMPLATE_SYNTAX_ERROR("2103", "error.template.syntax.error"),

    // 收件人组相关错误码 (2200-2299)
    GROUP_NOT_FOUND("2201", "error.group.not.found"),
    GROUP_CODE_EXISTS("2202", "error.group.code.exists"),
    GROUP_MEMBER_NOT_FOUND("2203", "error.group.member.not.found"),
    GROUP_MEMBER_EXISTS("2204", "error.group.member.exists"),

    // 通知记录相关错误码 (2300-2399)
    NOTIFICATION_NOT_FOUND("2301", "error.notification.not.found"),
    NOTIFICATION_CANNOT_RESEND("2302", "error.notification.cannot.resend"),
    NOTIFICATION_RESEND_FAILED("2303", "error.notification.resend.failed"),

    // 发送器相关错误码 (2400-2499)
    EMAIL_ADDRESS_EMPTY("2401", "error.email.address.empty"),
    EMAIL_CONTENT_EMPTY("2402", "error.email.content.empty"),
    EMAIL_PROVIDER_UNAVAILABLE("2403", "error.email.provider.unavailable"),
    SMS_PHONE_EMPTY("2404", "error.sms.phone.empty"),
    SMS_CONTENT_EMPTY("2405", "error.sms.content.empty"),
    SMS_PROVIDER_UNAVAILABLE("2406", "error.sms.provider.unavailable"),
    IM_ACCOUNT_EMPTY("2407", "error.im.account.empty"),
    IM_CONTENT_EMPTY("2408", "error.im.content.empty"),
    IM_PROVIDER_UNAVAILABLE("2409", "error.im.provider.unavailable"),
    INAPP_USER_ID_EMPTY("2410", "error.inapp.user.id.empty"),
    INAPP_CONTENT_EMPTY("2411", "error.inapp.content.empty"),
    INAPP_SAVE_FAILED("2412", "error.inapp.save.failed"),

    // 客户端错误码 (3000-3999)
    CLIENT_ERROR("3001", "error.client"),
    CLIENT_TIMEOUT("3002", "error.client.timeout"),
    CLIENT_CONNECTION_FAILED("3003", "error.client.connection.failed");

    private final String code;
    private final String messageKey;

    ErrorCode(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String toString() {
        return code + ":" + messageKey;
    }
}
