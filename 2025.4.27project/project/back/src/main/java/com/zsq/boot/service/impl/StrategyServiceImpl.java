package com.zsq.boot.service.impl;


import com.zsq.boot.entity.GateAction;
import com.zsq.boot.entity.Strategy;
import com.zsq.boot.service.StrategyService;
import com.zsq.boot.service.WaterLevelService;
import com.zsq.boot.entity.HydroData;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zsq.boot.entity.StrategyGenDTO;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class StrategyServiceImpl implements StrategyService {

    @Autowired
    private WaterLevelService waterLevelService;

    @Override
    public List<Strategy> generateStrategies(StrategyGenDTO dto) {
        log.info("开始生成策略，测站：{}，目标值：{}", dto.getStationName(), dto.getTargetValue());
        
        List<Strategy> strategies = new ArrayList<>();

        // 根据目标值范围生成不同策略
        if (dto.getTargetValue() <= 0.8) {
            // 目标值较低，生成引水策略
            strategies.add(createLowWaterStrategy(dto));
        } else if (dto.getTargetValue() >= 1.2) {
            // 目标值较高，生成泄洪策略
            strategies.add(createHighWaterStrategy(dto));
        } else {
            // 目标值适中，生成平衡策略
            strategies.add(createBalanceStrategy(dto));
        }

        return strategies;
    }

    // 创建低水位策略（引水）
    private Strategy createLowWaterStrategy(StrategyGenDTO dto) {
        Strategy strategy = new Strategy();
        strategy.setId(generateStrategyId());
        strategy.setTitle("策略：" + dto.getStationName() + "引水方案");
        strategy.setType("引水");
        strategy.setPriority("高");
        strategy.setStatus("推荐");
        strategy.setDescription(String.format(
            "针对%s测站水位降低（目标值%.0f%%）的调度策略，通过开启下游水闸降低水位",
            dto.getStationName(),
            dto.getTargetValue() * 100
        ));
        strategy.setSimulating(false);

        // 根据不同测站设置不同的水闸操作
        List<GateAction> actions = new ArrayList<>();
        switch(dto.getStationName()) {
            case "大舜":
            case "陶庄":
                // 上游测站，关闭上游闸，开启下游闸
                actions.addAll(Arrays.asList(
                    new GateAction(1, "关闭"),
                    new GateAction(2, "关闭"),
                    new GateAction(3, "开启"),
                    new GateAction(4, "开启"),
                    new GateAction(5, "关闭"),
                    new GateAction(6, "关闭")
                ));
                break;
            case "池家浜":
            case "天凝":
                // 中游测站，开启中下游闸
                actions.addAll(Arrays.asList(
                    new GateAction(1, "关闭"),
                    new GateAction(2, "关闭"),
                    new GateAction(3, "开启"),
                    new GateAction(4, "开启"),
                    new GateAction(5, "开启"),
                    new GateAction(6, "关闭")
                ));
                break;
            case "西塘":
            case "横港大桥":
                // 下游测站，开启所有下游闸
                actions.addAll(Arrays.asList(
                    new GateAction(1, "关闭"),
                    new GateAction(2, "关闭"),
                    new GateAction(3, "关闭"),
                    new GateAction(4, "开启"),
                    new GateAction(5, "开启"),
                    new GateAction(6, "开启")
                ));
                break;
        }
        strategy.setActions(actions);
        return strategy;
    }

    // 创建高水位策略（泄洪）
    private Strategy createHighWaterStrategy(StrategyGenDTO dto) {
        Strategy strategy = new Strategy();
        strategy.setId(generateStrategyId());
        strategy.setTitle("策略：" + dto.getStationName() + "泄洪方案");
        strategy.setType("泄洪");
        strategy.setPriority("高");
        strategy.setStatus("推荐");
        strategy.setDescription(String.format(
            "针对%s测站水位提高（目标值%.0f%%）的调度策略，通过开启上游水闸提高水位",
            dto.getStationName(),
            dto.getTargetValue() * 100
        ));
        strategy.setSimulating(false);

        // 根据不同测站设置不同的水闸操作
        List<GateAction> actions = new ArrayList<>();
        switch(dto.getStationName()) {
            case "大舜":
            case "陶庄":
                // 上游测站，开启所有上游闸
                actions.addAll(Arrays.asList(
                    new GateAction(1, "开启"),
                    new GateAction(2, "开启"),
                    new GateAction(3, "开启"),
                    new GateAction(4, "关闭"),
                    new GateAction(5, "关闭"),
                    new GateAction(6, "关闭")
                ));
                break;
                case "池家浜":
                case "天凝":
                    actions.addAll(Arrays.asList(
                        new GateAction(1, "开启"),
                        new GateAction(2, "开启"),
                        new GateAction(3, "关闭"),
                        new GateAction(4, "关闭"),
                        new GateAction(5, "关闭"),
                        new GateAction(6, "关闭")
                    ));
                    break;
                case "西塘":
                case "横港大桥":
                    actions.addAll(Arrays.asList(
                        new GateAction(1, "开启"),
                        new GateAction(2, "开启"),
                        new GateAction(3, "关闭"),
                        new GateAction(4, "关闭"),
                        new GateAction(5, "关闭"),
                        new GateAction(6, "关闭")
                    ));
                    break;
        }
        strategy.setActions(actions);
        return strategy;
    }

    // 创建平衡策略
    private Strategy createBalanceStrategy(StrategyGenDTO dto) {
        Strategy strategy = new Strategy();
        strategy.setId(generateStrategyId());
        strategy.setTitle("策略：" + dto.getStationName() + "平衡方案");
        strategy.setType("平衡");
        strategy.setPriority("中");
        strategy.setStatus("推荐");
        strategy.setDescription(String.format(
            "针对%s测站水位适中（目标值%.0f%%）的平衡策略，通过调节上下游水闸维持水位",
            dto.getStationName(),
            dto.getTargetValue() * 100
        ));
        strategy.setSimulating(false);

        // 设置平衡策略的水闸操作
        List<GateAction> actions = new ArrayList<>();
        // ... 根据测站位置设置相应的水闸操作
        switch(dto.getStationName()) {
            case "大舜":
            case "陶庄":
                actions.addAll(Arrays.asList(
                    new GateAction(1, "关闭"),  // 上游闸1关闭
                    new GateAction(2, "关闭"),  // 上游闸2关闭
                    new GateAction(3, "开启"),  // 中游闸3开启
                    new GateAction(4, "开启"),  // 中游闸4开启
                    new GateAction(5, "关闭"),  // 下游闸5关闭
                    new GateAction(6, "关闭")   // 下游闸6关闭
                ));
                break;
            case "池家浜":
            case "天凝":
                actions.addAll(Arrays.asList(
                    new GateAction(1, "关闭"),  // 上游闸1关闭
                    new GateAction(2, "关闭"),  // 上游闸2关闭
                    new GateAction(3, "开启"),  // 中游闸3开启
                    new GateAction(4, "开启"),  // 中游闸4开启
                    new GateAction(5, "开启"),  // 下游闸5开启
                    new GateAction(6, "关闭")   // 下游闸6关闭
                ));
                break;
            case "西塘":
            case "横港大桥":
                actions.addAll(Arrays.asList(
                    new GateAction(1, "关闭"),  // 上游闸1关闭
                    new GateAction(2, "关闭"),  // 上游闸2关闭
                    new GateAction(3, "关闭"),  // 中游闸3关闭
                    new GateAction(4, "开启"),  // 中游闸4开启
                    new GateAction(5, "开启"),  // 下游闸5开启
                    new GateAction(6, "开启")   // 下游闸6开启
                ));
                break;
        }
        strategy.setActions(actions);
        return strategy;
    }

    private Integer generateStrategyId() {
        // 生成唯一策略ID的逻辑
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    @Override
    public Map<String, Object> simulateStrategy(Map<String, Object> strategy) {
        //前端传过来的数据包含三部分：策略strategy、站名stationName、目标值targetValue
        log.info("开始模拟策略：{}", strategy);
        Map<String, Object> result = new HashMap<>();
        //根据stationName获取该测站最新水位信息
        //调用waterLevelService根据stationName获取最新水位，然后根据targetValue计算得到预计值
        String stationName = strategy.get("stationName").toString();
        //Double targetValue = (Double)strategy.get("targetValue");//会报类型转换错误，大舜 200%，因为可能被序列化为2
        Number number = (Number) strategy.get("targetValue");
        Double targetValue = number.doubleValue();
        HydroData hydroData = waterLevelService.getByStationName(stationName);
        //计算预计水位
        double randomFactor = ThreadLocalRandom.current().nextDouble(0.90, 1.00);//生成一个随机值，用于模拟预计水位
        Double predictionWaterLevel = hydroData.getWaterLevel()*targetValue*randomFactor;
        int randomNumTime = ThreadLocalRandom.current().nextInt(1, 5);  // 生成随机时间

        try {
            // 模拟结果
            result.put("expectedWaterLevel", predictionWaterLevel);
            result.put("timeToReach", randomNumTime+"小时");
            result.put("energyCost", "中等");
            
        } catch (Exception e) {
            log.error("策略模拟失败", e);
            throw new RuntimeException("策略模拟失败：" + e.getMessage());
        }
        
        return result;
    }
}
