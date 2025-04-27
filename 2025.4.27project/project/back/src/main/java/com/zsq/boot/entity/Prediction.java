package com.zsq.boot.entity;
import lombok.Data;
import java.util.Map;
@Data
public class Prediction {
    private Map<String, Double> waterLevel; // 各区域水位变化预测
    private Map<String, Double> flowRate;  // 各河道流速变化预测
    private String riskLevel;     // 风险等级（低/中/高/极高）
    private String timeEstimate;  // 预计耗时（如"2小时"）
    private String evaluation;    // 综合评估文本
}