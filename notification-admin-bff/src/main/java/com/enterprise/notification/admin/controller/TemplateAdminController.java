package com.enterprise.notification.admin.controller;

import com.enterprise.notification.admin.dto.*;
import com.enterprise.notification.admin.dto.query.TemplateQueryRequest;
import com.enterprise.notification.admin.service.TemplateAdminService;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * 模板管理控制器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/templates")
@Validated
public class TemplateAdminController {

    @Autowired
    private TemplateAdminService templateAdminService;

    /**
     * 分页查询模板列表
     */
    @PostMapping("/query")
    public ResponseEntity<PageResult<TemplateDto>> getTemplates(@Valid @RequestBody TemplateQueryRequest request) {

        log.info("查询模板列表: current={}, size={}, templateCode={}, templateName={}, channelCode={}, isEnabled={}",
                request.getCurrent(), request.getSize(), request.getTemplateCode(),
                request.getTemplateName(), request.getChannelCode(), request.getIsEnabled());

        PageResult<TemplateDto> result = templateAdminService.getTemplates(
                request.getCurrent(), request.getSize(), request.getTemplateCode(),
                request.getTemplateName(), request.getChannelCode(), request.getIsEnabled());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取单个模板详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable Long id) {
        log.info("获取模板详情: id={}", id);

        TemplateDto template = templateAdminService.getTemplate(id);
        return ResponseEntity.ok(template);
    }

    /**
     * 创建新模板
     */
    @PostMapping
    public ResponseEntity<TemplateDto> createTemplate(@Valid @RequestBody TemplateCreateRequest request) {
        log.info("创建模板: templateCode={}, templateName={}", request.getTemplateCode(), request.getTemplateName());

        TemplateDto template = templateAdminService.createTemplate(request);
        return ResponseEntity.ok(template);
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateDto> updateTemplate(@PathVariable Long id, 
                                                     @Valid @RequestBody TemplateUpdateRequest request) {
        log.info("更新模板: id={}, templateName={}", id, request.getTemplateName());

        TemplateDto template = templateAdminService.updateTemplate(id, request);
        return ResponseEntity.ok(template);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        log.info("删除模板: id={}", id);

        templateAdminService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 测试发送模板
     */
    @PostMapping("/test-send")
    public ResponseEntity<SendNotificationResponse> testSendTemplate(@Valid @RequestBody TemplateTestSendRequest request) {
        log.info("测试发送模板: templateCode={}, userId={}", 
                request.getTemplateCode(), request.getTestRecipient().getUserId());

        SendNotificationResponse response = templateAdminService.testSendTemplate(request);
        return ResponseEntity.ok(response);
    }
}
