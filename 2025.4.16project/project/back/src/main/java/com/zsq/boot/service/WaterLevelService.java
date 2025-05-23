package com.zsq.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsq.boot.entity.WaterLevel;
import java.util.List;

public interface WaterLevelService extends IService<WaterLevel> {
    // 获取指定时间范围的水位数据
    List<WaterLevel> getRecentData(String timeRange, String location, String date);
} 