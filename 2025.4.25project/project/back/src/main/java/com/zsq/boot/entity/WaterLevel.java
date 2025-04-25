package com.zsq.boot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("water_level")
public class WaterLevel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("monitoring_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;  // 监测时间
    
    @TableField("xitang_main")
    private Float xitangMain;             // 西塘(主)
    
    @TableField("henggang_bridge_main")
    private Float henggangBridgeMain;     // 横港大桥(主)
    
    @TableField("taozhuang_main")
    private Float taozhuangMain;          // 陶庄(主)
    
    @TableField("dingzha_main")
    private Float dingshaMain;            // 丁栅（主）
    
    @TableField("chijia_bang_main")
    private Float chijiaBangMain;         // 池家浜(主)
    
    @TableField("tianning_main")
    private Float tianningMain;           // 天凝（主）
    
    @TableField("dashun")
    private Float dashun;                 // 大舜
} 