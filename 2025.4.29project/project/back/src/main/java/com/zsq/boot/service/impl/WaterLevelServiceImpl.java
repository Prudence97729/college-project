package com.zsq.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsq.boot.entity.HydroData;
import com.zsq.boot.entity.WaterLevel;
import com.zsq.boot.mapper.WaterLevelMapper;
import com.zsq.boot.service.WaterLevelService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Service
public class WaterLevelServiceImpl extends ServiceImpl<WaterLevelMapper, WaterLevel> implements WaterLevelService {
    // 测站名称到数据库字段的映射,用户前端传递过来的水位名称找到相应的数据库表字段
    private static final Map<String, String> STATION_COLUMN_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("西塘", "xitang_main");
        map.put("横港大桥", "henggang_bridge_main");
        map.put("陶庄", "taozhuang_main");
        map.put("丁栅", "dingsha_main");
        map.put("池家浜", "chijia_bang_main");
        map.put("天凝", "tianning_main");
        map.put("大舜", "dashun");
        STATION_COLUMN_MAP = Collections.unmodifiableMap(map); // 转为不可变Map
    }

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

    @Override
    public HydroData getByStationName(String stationName){
        //获取映射信息
        String columnName = getColumnNameByStation(stationName);
        if(columnName == null){
            throw new IllegalArgumentException("无效的测站名称: " + stationName);
        }

        //构建查询
        QueryWrapper<WaterLevel> wrapper = new QueryWrapper<>();
        wrapper.select("monitoring_time", columnName) // 只查询时间和对应测站的水位值
              .orderByDesc("monitoring_time")        // 按时间降序
              .last("LIMIT 1");                      // 取最新一条        
        
        //进行查询
        Map<String, Object> resultMap = getMap(wrapper);
        if (resultMap == null || resultMap.isEmpty()) {
            throw new RuntimeException(stationName + "测站无数据");
        }

        //数据格式转换
        HydroData hydroData = new HydroData();
        try {
            hydroData.setLocation(stationName);
            hydroData.setTimestamp((LocalDateTime) resultMap.get("monitoring_time"));
            
            // 处理可能为null的水位值
            Object waterLevelValue = resultMap.get(columnName);
            if (waterLevelValue != null) {
                hydroData.setWaterLevel(((Number) waterLevelValue).doubleValue());
            } else {
                hydroData.setWaterLevel(0.0); // 默认值或抛出异常
            }
            
        } catch (ClassCastException e) {
            throw new RuntimeException("数据格式错误", e);
        }

        return hydroData;
    }

    // 辅助方法：测站名称到列名的映射
    private String getColumnNameByStation(String stationName) {
        switch (stationName) {
            case "西塘": return "xitang_main";
            case "横港大桥": return "henggang_bridge_main";
            case "陶庄": return "taozhuang_main";
            case "丁栅": return "dingsha_main";
            case "池家浜": return "chijia_bang_main";
            case "天凝": return "tianning_main";
            case "大舜": return "dashun";
            default: return null;
        }
    }

    @Override//获取指定小时的环境水位信息
    public List<HydroData> getEnvData(String stationName, Integer hours) {
        //1.检验有效性
        if(StringUtils.isBlank(stationName)){
            throw new IllegalArgumentException("测站名称不能为空");
        }
        if (hours == null || hours <= 0) {
            throw new IllegalArgumentException("时间范围必须大于0");
        }
        //2.获取列名映射
        String columnName = getColumnNameByStation(stationName);
        if (columnName == null) {
            throw new IllegalArgumentException("不支持的测站名称: " + stationName);
        }
        //3.从数据库获取最新数据（最后一条数据）作为最后一条时间数据
        QueryWrapper<WaterLevel> latestQuery = new QueryWrapper<>();
        latestQuery.select("monitoring_time")
            .isNotNull(columnName)
            .orderByDesc("monitoring_time")
            .last("LIMIT 1");
        Map<String, Object> latestMap = getMap(latestQuery);//获取最新的一条数据
        LocalDateTime endTime = (LocalDateTime)latestMap.get("monitoring_time");
        //hydroData.setTimestamp((LocalDateTime) resultMap.get("monitoring_time"));
        LocalDateTime startTime = endTime.minusHours(hours);
        //4.查询指定范围内的最新数据
        QueryWrapper<WaterLevel> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("monitoring_time", columnName)//示例："monitoring_time": "2024-09-13 00:10:00", "dashun": 3.21
            .between("monitoring_time", startTime, endTime)
            .isNotNull(columnName)
            .orderByDesc("monitoring_time");
        Map<String, Object> resultMap = getMap(queryWrapper);
        //5.返回结果
        List<HydroData> result = new ArrayList<>();//返回一个HrdroData数组
        List<Map<String, Object>> resultMaps = listMaps(queryWrapper);
        for (Map<String, Object> map : resultMaps) {
            HydroData data = new HydroData();
            data.setTimestamp((LocalDateTime) map.get("monitoring_time"));
            data.setWaterLevel(((Number) map.get(columnName)).doubleValue());
            data.setLocation(stationName);
            result.add(data);
        }
        return result;
    }
}
