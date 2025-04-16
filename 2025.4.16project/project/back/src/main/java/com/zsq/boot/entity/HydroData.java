package com.zsq.boot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HydroData {
    private LocalDateTime timestamp;  // 时间戳
    private Double waterLevel;        // 水位
    private String location;          // 测点位置
} 