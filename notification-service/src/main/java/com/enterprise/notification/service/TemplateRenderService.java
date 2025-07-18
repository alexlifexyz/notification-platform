package com.enterprise.notification.service;

import com.enterprise.notification.config.NotificationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板渲染服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class TemplateRenderService {

    @Autowired
    private NotificationProperties notificationProperties;

    private Pattern variablePattern;

    /**
     * 初始化变量匹配模式
     */
    private Pattern getVariablePattern() {
        if (variablePattern == null) {
            String pattern = notificationProperties.getTemplate().getVariablePattern();
            variablePattern = Pattern.compile(pattern);
        }
        return variablePattern;
    }

    /**
     * 渲染模板内容
     *
     * @param template 模板内容
     * @param params   参数映射
     * @return 渲染后的内容
     */
    public String render(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return template;
        }

        if (params == null || params.isEmpty()) {
            log.warn("模板参数为空，返回原始模板内容");
            return template;
        }

        try {
            String result = template;
            Pattern pattern = getVariablePattern();
            Matcher matcher = pattern.matcher(template);

            while (matcher.find()) {
                String placeholder = matcher.group(0); // 完整的占位符，如 ${userName}
                String variableName = matcher.group(1); // 变量名，如 userName

                Object value = params.get(variableName);
                String replacement = value != null ? value.toString() : "";

                result = result.replace(placeholder, replacement);
                
                log.debug("替换模板变量: {} -> {}", placeholder, replacement);
            }

            return result;
        } catch (Exception e) {
            log.error("模板渲染失败: template={}, params={}", template, params, e);
            throw new RuntimeException("模板渲染失败", e);
        }
    }

    /**
     * 验证模板语法
     *
     * @param template 模板内容
     * @return 是否有效
     */
    public boolean validateTemplate(String template) {
        if (!StringUtils.hasText(template)) {
            return true;
        }

        try {
            Pattern pattern = getVariablePattern();
            Matcher matcher = pattern.matcher(template);
            
            // 检查是否有未闭合的占位符
            while (matcher.find()) {
                String variableName = matcher.group(1);
                if (!StringUtils.hasText(variableName)) {
                    log.warn("发现空的变量名: {}", matcher.group(0));
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("模板语法验证失败: {}", template, e);
            return false;
        }
    }

    /**
     * 提取模板中的变量名
     *
     * @param template 模板内容
     * @return 变量名集合
     */
    public java.util.Set<String> extractVariables(String template) {
        java.util.Set<String> variables = new java.util.HashSet<>();
        
        if (!StringUtils.hasText(template)) {
            return variables;
        }

        try {
            Pattern pattern = getVariablePattern();
            Matcher matcher = pattern.matcher(template);
            
            while (matcher.find()) {
                String variableName = matcher.group(1);
                if (StringUtils.hasText(variableName)) {
                    variables.add(variableName);
                }
            }
        } catch (Exception e) {
            log.error("提取模板变量失败: {}", template, e);
        }

        return variables;
    }
}
