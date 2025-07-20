package com.enterprise.notification.common.util;

import com.enterprise.notification.common.enums.ErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息工具类
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Component
public class MessageUtils {

    private static MessageSource messageSource;

    public MessageUtils(MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    /**
     * 获取国际化消息
     *
     * @param key 消息键
     * @return 国际化消息
     */
    public static String getMessage(String key) {
        return getMessage(key, null);
    }

    /**
     * 获取国际化消息
     *
     * @param key  消息键
     * @param args 参数
     * @return 国际化消息
     */
    public static String getMessage(String key, Object[] args) {
        return getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * 获取国际化消息
     *
     * @param key    消息键
     * @param args   参数
     * @param locale 语言环境
     * @return 国际化消息
     */
    public static String getMessage(String key, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            return key; // 如果找不到消息，返回key本身
        }
    }

    /**
     * 根据错误码获取国际化消息
     *
     * @param errorCode 错误码
     * @return 国际化消息
     */
    public static String getMessage(ErrorCode errorCode) {
        return getMessage(errorCode.getMessageKey());
    }

    /**
     * 根据错误码获取国际化消息
     *
     * @param errorCode 错误码
     * @param args      参数
     * @return 国际化消息
     */
    public static String getMessage(ErrorCode errorCode, Object[] args) {
        return getMessage(errorCode.getMessageKey(), args);
    }

    /**
     * 获取成功消息
     *
     * @return 成功消息
     */
    public static String getSuccessMessage() {
        return getMessage(ErrorCode.SUCCESS);
    }

    /**
     * 获取当前语言环境
     *
     * @return 当前语言环境
     */
    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * 判断是否为中文环境
     *
     * @return 是否为中文环境
     */
    public static boolean isChineseLocale() {
        Locale locale = getCurrentLocale();
        return Locale.CHINESE.getLanguage().equals(locale.getLanguage()) ||
               Locale.CHINA.equals(locale);
    }

    /**
     * 判断是否为英文环境
     *
     * @return 是否为英文环境
     */
    public static boolean isEnglishLocale() {
        Locale locale = getCurrentLocale();
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ||
               Locale.US.equals(locale);
    }
}
