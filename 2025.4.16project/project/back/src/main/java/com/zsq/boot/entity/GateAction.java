package com.zsq.boot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GateAction {
    private Integer gateId;  // 水闸ID
    private String action;   // 操作类型（开启/关闭）
}