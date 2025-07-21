package com.enterprise.notification.admin.controller;

import com.enterprise.notification.admin.dto.channel.ChannelCreateRequest;
import com.enterprise.notification.admin.dto.channel.ChannelDto;
import com.enterprise.notification.admin.dto.channel.ChannelUpdateRequest;
import com.enterprise.notification.admin.service.ChannelAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 渠道管理控制器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/channels")
@Validated
@Tag(name = "渠道管理", description = "通知渠道的增删改查和状态管理功能")
public class ChannelAdminController {

    @Autowired
    private ChannelAdminService channelAdminService;

    /**
     * 查询所有渠道
     */
    @Operation(summary = "查询所有渠道", description = "获取系统中所有通知渠道列表")
    @GetMapping
    public ResponseEntity<List<ChannelDto>> getAllChannels() {
        log.info("查询所有渠道");

        List<ChannelDto> channels = channelAdminService.getAllChannels();
        return ResponseEntity.ok(channels);
    }

    /**
     * 获取渠道详情
     */
    @Operation(summary = "获取渠道详情", description = "根据渠道代码获取渠道详细信息")
    @GetMapping("/{channelCode}")
    public ResponseEntity<ChannelDto> getChannel(@PathVariable String channelCode) {
        log.info("获取渠道详情: channelCode={}", channelCode);

        ChannelDto channel = channelAdminService.getChannel(channelCode);
        return ResponseEntity.ok(channel);
    }

    /**
     * 创建渠道
     */
    @Operation(summary = "创建渠道", description = "创建新的通知渠道")
    @PostMapping
    public ResponseEntity<ChannelDto> createChannel(@Valid @RequestBody ChannelCreateRequest request) {
        log.info("创建渠道: channelCode={}, channelName={}", 
                request.getChannelCode(), request.getChannelName());

        ChannelDto channel = channelAdminService.createChannel(request);
        return ResponseEntity.ok(channel);
    }

    /**
     * 更新渠道
     */
    @Operation(summary = "更新渠道", description = "更新渠道信息")
    @PutMapping("/{channelCode}")
    public ResponseEntity<ChannelDto> updateChannel(@PathVariable String channelCode,
                                                   @Valid @RequestBody ChannelUpdateRequest request) {
        log.info("更新渠道: channelCode={}, channelName={}", channelCode, request.getChannelName());

        ChannelDto channel = channelAdminService.updateChannel(channelCode, request);
        return ResponseEntity.ok(channel);
    }

    /**
     * 启用/禁用渠道
     */
    @Operation(summary = "切换渠道状态", description = "启用或禁用指定渠道")
    @PutMapping("/{channelCode}/toggle-status")
    public ResponseEntity<ChannelDto> toggleChannelStatus(@PathVariable String channelCode) {
        log.info("切换渠道状态: channelCode={}", channelCode);

        ChannelDto channel = channelAdminService.toggleChannelStatus(channelCode);
        return ResponseEntity.ok(channel);
    }

    /**
     * 删除渠道
     */
    @Operation(summary = "删除渠道", description = "删除指定的通知渠道")
    @DeleteMapping("/{channelCode}")
    public ResponseEntity<Void> deleteChannel(@PathVariable String channelCode) {
        log.info("删除渠道: channelCode={}", channelCode);

        channelAdminService.deleteChannel(channelCode);
        return ResponseEntity.ok().build();
    }
}
