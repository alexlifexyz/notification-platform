package com.enterprise.notification.admin.controller;

import com.enterprise.notification.admin.dto.common.PageResult;
import com.enterprise.notification.admin.dto.recipient.*;
import com.enterprise.notification.admin.service.RecipientAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 收件人管理控制器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/recipients")
@Validated
@Tag(name = "收件人管理", description = "收件人的增删改查和批量导入功能")
public class RecipientAdminController {

    @Autowired
    private RecipientAdminService recipientAdminService;

    /**
     * 分页查询收件人
     */
    @Operation(summary = "分页查询收件人", description = "支持按用户ID、姓名、手机号、邮箱、组等条件分页查询收件人")
    @PostMapping("/query")
    public ResponseEntity<PageResult<RecipientDto>> getRecipients(
            @Parameter(description = "查询条件") @Valid @RequestBody RecipientQueryRequest request) {

        log.info("查询收件人列表: current={}, size={}, userId={}, userName={}, groupCode={}",
                request.getCurrent(), request.getSize(), request.getUserId(),
                request.getUserName(), request.getGroupCode());

        PageResult<RecipientDto> result = recipientAdminService.getRecipients(
                request.getCurrent(), request.getSize(), request.getUserId(),
                request.getUserName(), request.getPhone(), request.getEmail(),
                request.getGroupCode(), request.getIsEnabled());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取收件人详情
     */
    @Operation(summary = "获取收件人详情", description = "根据用户ID获取收件人详细信息")
    @GetMapping("/{userId}")
    public ResponseEntity<RecipientDto> getRecipient(@PathVariable String userId) {
        log.info("获取收件人详情: userId={}", userId);

        RecipientDto recipient = recipientAdminService.getRecipient(userId);
        return ResponseEntity.ok(recipient);
    }

    /**
     * 创建收件人
     */
    @Operation(summary = "创建收件人", description = "创建新的收件人")
    @PostMapping
    public ResponseEntity<RecipientDto> createRecipient(@Valid @RequestBody RecipientCreateRequest request) {
        log.info("创建收件人: userId={}, userName={}, groupCode={}", 
                request.getUserId(), request.getUserName(), request.getGroupCode());

        RecipientDto recipient = recipientAdminService.createRecipient(request);
        return ResponseEntity.ok(recipient);
    }

    /**
     * 更新收件人
     */
    @Operation(summary = "更新收件人", description = "更新收件人信息")
    @PutMapping("/{userId}")
    public ResponseEntity<RecipientDto> updateRecipient(@PathVariable String userId,
                                                       @Valid @RequestBody RecipientUpdateRequest request) {
        log.info("更新收件人: userId={}, userName={}", userId, request.getUserName());

        RecipientDto recipient = recipientAdminService.updateRecipient(userId, request);
        return ResponseEntity.ok(recipient);
    }

    /**
     * 删除收件人
     */
    @Operation(summary = "删除收件人", description = "删除指定的收件人")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteRecipient(@PathVariable String userId) {
        log.info("删除收件人: userId={}", userId);

        recipientAdminService.deleteRecipient(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量导入收件人
     */
    @Operation(summary = "批量导入收件人", description = "批量导入收件人到指定组")
    @PostMapping("/batch-import")
    public ResponseEntity<List<RecipientDto>> batchImportRecipients(@Valid @RequestBody RecipientBatchImportRequest request) {
        log.info("批量导入收件人: groupCode={}, count={}", 
                request.getGroupCode(), request.getRecipients().size());

        List<RecipientDto> recipients = recipientAdminService.batchImportRecipients(request);
        return ResponseEntity.ok(recipients);
    }
}
