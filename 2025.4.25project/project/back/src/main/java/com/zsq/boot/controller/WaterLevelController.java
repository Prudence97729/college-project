package com.zsq.boot.controller;

import com.zsq.boot.common.Result;
import com.zsq.boot.service.WaterLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/water-level")
public class WaterLevelController {
    
    @Autowired
    private WaterLevelService waterLevelService;
    
    @GetMapping("/data")
    public Result getWaterLevelData(
            @RequestParam(defaultValue = "all") String timeRange,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String date) {
        try {
            return Result.success("获取成功", waterLevelService.getRecentData(timeRange, location, date));
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }
} 