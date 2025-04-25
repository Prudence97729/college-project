package com.zsq.boot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("gate")
public class Gate {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("gate_name")
    private String gateName;        // 水闸名称
    @TableField("gate_code")
    private String gateCode;        // 闸门编号
    @TableField("device_type")
    private String deviceType;      // 闸门类型
    @TableField("gate_count")
    private Integer gateCount;      // 闸门数量
    private Float width;           // 闸门宽度
    @TableField("sill_elevation")
    private Float sillElevation;   // 闸底高程
    @TableField("gate_height")
    private Float gateHeight;      // 闸门高度
    @TableField("flow_coefficient")
    private Float flowCoefficient; // 流量系数
    private String status;          // 运行状态
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 