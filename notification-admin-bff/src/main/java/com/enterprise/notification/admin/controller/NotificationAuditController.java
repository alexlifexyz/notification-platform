package com.enterprise.notification.admin.controller;

import com.enterprise.notification.admin.dto.common.PageResult;
import com.enterprise.notification.admin.dto.notification.*;
import com.enterprise.notification.admin.service.NotificationAuditService;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 通知审计监控控制器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@Validated
@Tag(name = "通知审计监控", description = "通知记录查询、统计分析和重发功能")
public class NotificationAuditController {

    @Autowired
    private NotificationAuditService notificationAuditService;

    /**
     * 分页查询通知记录
     */
    @PostMapping("/query")
    public ResponseEntity<PageResult<NotificationRecordDto>> getNotificationRecords(@Valid @RequestBody NotificationRecordQueryRequest request) {

        log.info("查询通知记录: current={}, size={}, requestId={}, templateCode={}, recipientId={}, status={}, startTime={}, endTime={}",
                request.getCurrent(), request.getSize(), request.getRequestId(), request.getTemplateCode(),
                request.getRecipientId(), request.getStatus(), request.getStartTime(), request.getEndTime());

        PageResult<NotificationRecordDto> result = notificationAuditService.getNotificationRecords(
                request.getCurrent(), request.getSize(), request.getRequestId(), request.getTemplateCode(),
                request.getRecipientId(), request.getStatus(), request.getStartTime(), request.getEndTime());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取通知记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationRecordDto> getNotificationRecord(@PathVariable Long id) {
        log.info("获取通知记录详情: id={}", id);

        NotificationRecordDto record = notificationAuditService.getNotificationRecord(id);
        return ResponseEntity.ok(record);
    }

    /**
     * 重发失败通知
     */
    @PostMapping("/{id}/resend")
    public ResponseEntity<SendNotificationResponse> resendNotification(@PathVariable Long id,
                                                                      @Valid @RequestBody NotificationResendRequest request) {
        log.info("重发通知: id={}, reason={}", id, request.getReason());

        SendNotificationResponse response = notificationAuditService.resendNotification(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取通知统计
     */
    @PostMapping("/statistics")
    public ResponseEntity<NotificationStatisticsDto> getNotificationStatistics(@Valid @RequestBody NotificationStatisticsQueryRequest request) {

        log.info("获取通知统计: startTime={}, endTime={}, groupBy={}",
                request.getStartTime(), request.getEndTime(), request.getGroupBy());

        NotificationStatisticsDto statistics = notificationAuditService.getNotificationStatistics(
                request.getStartTime(), request.getEndTime(), request.getGroupBy());

        return ResponseEntity.ok(statistics);
    }
}
