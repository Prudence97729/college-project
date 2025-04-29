package com.zsq.boot.entity;

import lombok.Data;
import java.util.List;
@Data
public class Strategy {
    private Integer id;
    private String title;
    private String type;
    private String priority;
    private String status;
    private String description;
    private List<GateAction> actions;
    private Prediction prediction;
    private Boolean simulating;
}