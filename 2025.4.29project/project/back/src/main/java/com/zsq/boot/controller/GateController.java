package com.zsq.boot.controller;

import com.zsq.boot.common.Result;
import com.zsq.boot.entity.Gate;
import com.zsq.boot.service.GateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gates")
public class GateController {
    
    private static final Logger log = LoggerFactory.getLogger(GateController.class);
    
    @Autowired
    private GateService gateService;
    
    @GetMapping
    public Result getAllGates() {
        try {
            log.debug("=== 开始获取水闸列表 ===");
            List<Gate> gates = gateService.list();
            log.debug("获取到 {} 个水闸", gates.size());
            return Result.success("获取成功", gates);
        } catch (Exception e) {
            log.error("获取水闸列表失败", e);
            return Result.error("获取水闸列表失败");
        }
    }
    
    @GetMapping("/{id}")    
    public Result getGateById(@PathVariable Long id) {
        Gate gate = gateService.getById(id);
        return gate != null ? 
            Result.success("获取水闸信息成功", gate) : 
            Result.error("水闸不存在");
    }
    
    @PutMapping("/{id}/status")
    public Result updateGateStatus(
        @PathVariable Long id,
        @RequestParam String status
    ) {
        try {
            log.debug("正在更新水闸状态，id: {}, status: {}", id, status);
            Gate gate = gateService.getById(id);
            if (gate == null) {
                return Result.error("水闸不存在");
            }
            
            gate.setStatus(status);
            gate.setUpdateTime(LocalDateTime.now());
            boolean success = gateService.updateById(gate);
            
            if (success) {
                return Result.success("更新水闸状态成功");
            } else {
                return Result.error("更新水闸状态失败");
            }
        } catch (Exception e) {
            log.error("更新水闸状态失败", e);
            return Result.error("更新水闸状态失败: " + e.getMessage());
        }
    }
} 