package com.zsq.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsq.boot.entity.WaterLevel;
import com.zsq.boot.mapper.WaterLevelMapper;
import com.zsq.boot.service.WaterLevelService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Service
public class WaterLevelServiceImpl extends ServiceImpl<WaterLevelMapper, WaterLevel> implements WaterLevelService {
    
    @Override
    public List<WaterLevel> getRecentData(String timeRange, String location, String date) {
        // 先获取最新的记录时间
        WaterLevel latest = lambdaQuery()
                .orderByDesc(WaterLevel::getMonitoringTime)
                .last("LIMIT 1")
                .one();
                
        if (latest == null) {
            return new ArrayList<>();
        }
        
        LocalDateTime endTime;
        LocalDateTime startTime;
        
        // 根据时间范围参数设置查询条件
        switch (timeRange) {
            case "1d":
                // 如果提供了日期，使用该日期
                if (date != null && !date.isEmpty()) {
                    LocalDate selectedDate = LocalDate.parse(date);
                    startTime = selectedDate.atStartOfDay();
                    endTime = selectedDate.plusDays(1).atStartOfDay().minusSeconds(1);
                } else {
                    // 默认使用最后一天
                    endTime = latest.getMonitoringTime();
                    startTime = endTime.toLocalDate().atStartOfDay();
                }
                break;
            case "3d":
                if (date != null && !date.isEmpty()) {
                    LocalDate selectedDate = LocalDate.parse(date);
                    startTime = selectedDate.atStartOfDay();
                    endTime = selectedDate.plusDays(3).atStartOfDay().minusSeconds(1);
                } else {
                    endTime = latest.getMonitoringTime();
                    startTime = endTime.minusDays(3);
                }
                break;
            case "all":
                endTime = latest.getMonitoringTime();
                // 获取最早的记录时间
                WaterLevel earliest = lambdaQuery()
                        .orderByAsc(WaterLevel::getMonitoringTime)
                        .last("LIMIT 1")
                        .one();
                startTime = earliest != null ? earliest.getMonitoringTime() : endTime.minusDays(30);
                break;
            default:
                endTime = latest.getMonitoringTime();
                startTime = endTime.minusHours(24);
        }
        
        // 构建查询条件
        return lambdaQuery()
                .ge(WaterLevel::getMonitoringTime, startTime)
                .le(WaterLevel::getMonitoringTime, endTime)
                .orderByAsc(WaterLevel::getMonitoringTime)
                .list();
    }
} 