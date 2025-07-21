package com.enterprise.notification.admin.controller;

import com.enterprise.notification.admin.dto.common.PageResult;
import com.enterprise.notification.admin.dto.message.InAppMessageBatchOperationRequest;
import com.enterprise.notification.admin.dto.message.InAppMessageDto;
import com.enterprise.notification.admin.dto.message.InAppMessageQueryRequest;
import com.enterprise.notification.admin.dto.message.InAppMessageStatisticsDto;
import com.enterprise.notification.admin.service.InAppMessageAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 站内信管理控制器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/in-app-messages")
@Validated
@Tag(name = "站内信管理", description = "站内信的查询、标记已读/未读、删除和统计功能")
public class InAppMessageAdminController {

    @Autowired
    private InAppMessageAdminService inAppMessageAdminService;

    /**
     * 分页查询站内信
     */
    @Operation(summary = "分页查询站内信", description = "支持按用户ID、主题、已读状态、时间范围等条件分页查询站内信")
    @PostMapping("/query")
    public ResponseEntity<PageResult<InAppMessageDto>> getInAppMessages(
            @Parameter(description = "查询条件") @Valid @RequestBody InAppMessageQueryRequest request) {

        log.info("查询站内信列表: current={}, size={}, userId={}, subject={}, isRead={}",
                request.getCurrent(), request.getSize(), request.getUserId(),
                request.getSubject(), request.getIsRead());

        PageResult<InAppMessageDto> result = inAppMessageAdminService.getInAppMessages(
                request.getCurrent(), request.getSize(), request.getUserId(),
                request.getSubject(), request.getIsRead(), 
                request.getStartTime(), request.getEndTime());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取站内信详情
     */
    @Operation(summary = "获取站内信详情", description = "根据ID获取站内信详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<InAppMessageDto> getInAppMessage(@PathVariable Long id) {
        log.info("获取站内信详情: id={}", id);

        InAppMessageDto message = inAppMessageAdminService.getInAppMessage(id);
        return ResponseEntity.ok(message);
    }

    /**
     * 标记已读
     */
    @Operation(summary = "标记已读", description = "将指定站内信标记为已读")
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.info("标记站内信已读: id={}", id);

        inAppMessageAdminService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 标记未读
     */
    @Operation(summary = "标记未读", description = "将指定站内信标记为未读")
    @PutMapping("/{id}/mark-unread")
    public ResponseEntity<Void> markAsUnread(@PathVariable Long id) {
        log.info("标记站内信未读: id={}", id);

        inAppMessageAdminService.markAsUnread(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除站内信
     */
    @Operation(summary = "删除站内信", description = "删除指定的站内信")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInAppMessage(@PathVariable Long id) {
        log.info("删除站内信: id={}", id);

        inAppMessageAdminService.deleteInAppMessage(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量操作
     */
    @Operation(summary = "批量操作", description = "批量标记已读/未读或删除站内信")
    @PostMapping("/batch-operation")
    public ResponseEntity<Void> batchOperation(@Valid @RequestBody InAppMessageBatchOperationRequest request) {
        log.info("批量操作站内信: operation={}, count={}", 
                request.getOperation(), request.getMessageIds().size());

        inAppMessageAdminService.batchOperation(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取站内信统计
     */
    @Operation(summary = "获取站内信统计", description = "获取站内信的统计信息，包括总数、已读数、未读数等")
    @GetMapping("/statistics")
    public ResponseEntity<InAppMessageStatisticsDto> getStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) String userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        log.info("获取站内信统计: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        InAppMessageStatisticsDto statistics = inAppMessageAdminService.getStatistics(userId, startTime, endTime);
        return ResponseEntity.ok(statistics);
    }
}
