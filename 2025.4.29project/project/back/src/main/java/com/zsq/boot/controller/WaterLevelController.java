package com.zsq.boot.controller;

import com.zsq.boot.common.Result;
import com.zsq.boot.entity.HydroData;
import com.zsq.boot.service.WaterLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/water-level")
public class WaterLevelController {
    private static final Logger log = LoggerFactory.getLogger(WaterLevelController.class);

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
    
    @GetMapping("/latest")
    public Result getOneWaterLevel(@RequestParam String stationName){  
        try {
            HydroData result = waterLevelService.getByStationName(stationName);
            log.info("从{}获取到的结果：{}",stationName,result);
            return Result.success("获取成功",result);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/env")//获取环境测站水位信息
    public Result getEnvData(@RequestParam String stationName,@RequestParam Integer hours) {//大舜or横港，hours为所选时间范围：如6h
        try {
            return Result.success("环境水位获取成功", waterLevelService.getEnvData(stationName,hours));
        } catch (Exception e) {
            return Result.error("环境水位获取失败：" + e.getMessage());
        }
    }
} 
