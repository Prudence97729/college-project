package com.zsq.boot.entity;

import lombok.Data;

@Data
public class StrategyGenDTO {
    private Long stationId;//水位测站ID
    private String stationName;//水位测站名称
    private Double targetValue;//水位目标值
}
