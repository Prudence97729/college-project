package com.zsq.boot.entity;

import lombok.Data;

@Data
public class StrategyGenDTO {
    private Long stationId;//水位测站ID
    private String stationName;//水位测站名称
    private Double targetValue;//水位目标值
    private Integer dashunHours;//环境水位大舜水位值
    private Integer henggangHours;//环境水位横港水位值
}
