package com.zsq.boot.controller;

import com.zsq.boot.common.Result;
import com.zsq.boot.entity.Prediction;
import com.zsq.boot.entity.Strategy;
import com.zsq.boot.entity.StrategyGenDTO;
import com.zsq.boot.service.StrategyService;
import com.zsq.boot.entity.GateAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {
    private static final Logger log = LoggerFactory.getLogger(StrategyController.class);

    @Autowired
    private StrategyService strategyService;

    @PostMapping("/generate")
    public Result generateStrategy(@RequestBody StrategyGenDTO dto) {
        try {
            // 参数验证
            if (dto.getStationId() == null || dto.getStationName() == null || dto.getTargetValue() == null) {
                return Result.error("缺少必要参数");
            }
            
            log.info("接收到生成策略请求，测站：{}（ID: {}），目标值：{}，大舜小时{}，横港小时{}",
                    dto.getStationName(), dto.getStationId(), dto.getTargetValue(),dto.getDashunHours(),dto.getHenggangHours());
            
            // 调用服务层生成策略
            List<Strategy> strategies = strategyService.generateStrategies(dto);
            
            log.info("策略：{}",strategies);
            
            return Result.success("生成策略成功",strategies);
            
        } catch (IllegalArgumentException e) {
            log.error("参数错误", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("生成策略失败", e);
            return Result.error("生成策略失败：" + e.getMessage());
        }
    }

    @PostMapping("/simulate")
    public Result simulateStrategy(@RequestBody Map<String, Object> strategy) {
        //前端传过来的数据包含三部分：策略strategy、站名stationName、目标水位值targetValue
        try {
            log.info("接收到策略模拟请求：{}", strategy);//stationName=大舜, targetValue=5
            Thread.sleep(2000); // 模拟处理耗时
            // 调用服务层模拟策略
            Map<String, Object> simulationResult = strategyService.simulateStrategy(strategy);
            log.info("模拟结果：{}",simulationResult);
            return Result.success("策略模拟成功",simulationResult);
            
        } catch (Exception e) {
            log.error("策略模拟失败", e);
            return Result.error("策略模拟失败：" + e.getMessage());
        }
    }





    //老版本
    @GetMapping
    public Result getStrategies() {
        List<Strategy> strategies = createMockStrategies();
        return Result.success("获取成功", strategies);
    }
    
    
    private List<Strategy> createMockStrategies() {
        List<Strategy> strategies = new ArrayList<>();
        
        // 策略1: 上游泄洪
        Strategy strategy1 = new Strategy();
        strategy1.setId(1);
        strategy1.setTitle("策略一：上游泄洪");
        strategy1.setType("防洪");
        strategy1.setPriority("高");
        strategy1.setStatus("推荐");
        strategy1.setDescription("打开上游水闸，关闭下游水闸，用于上游水位过高时的泄洪");
        strategy1.setSimulating(false);
        
        List<GateAction> actions1 = Arrays.<GateAction>asList(
            new GateAction(1, "开启"),
            new GateAction(2, "开启"),
            new GateAction(3, "关闭"),
            new GateAction(4, "关闭"),
            new GateAction(5, "关闭"),
            new GateAction(6, "关闭")
        );
        strategy1.setActions(actions1);
        
        // 策略2: 下游引水
        Strategy strategy2 = new Strategy();
        strategy2.setId(2);
        strategy2.setTitle("策略二：下游引水");
        strategy2.setType("引水");
        strategy2.setPriority("中");
        strategy2.setStatus("备选");
        strategy2.setDescription("关闭上游水闸，打开下游水闸，用于下游水位过低时的引水");
        strategy2.setSimulating(false);
        strategy2.setActions(Arrays.asList(
            new GateAction(1, "关闭"),
            new GateAction(2, "关闭"),
            new GateAction(3, "关闭"),
            new GateAction(4, "开启"),
            new GateAction(5, "开启"),
            new GateAction(6, "开启")
        ));
        
        // 策略3: 平衡调节
        Strategy strategy3 = new Strategy();
        strategy3.setId(3);
        strategy3.setTitle("策略三：平衡调节");
        strategy3.setType("平衡");
        strategy3.setPriority("低");
        strategy3.setStatus("备选");
        strategy3.setDescription("部分水闸开启，部分水闸关闭，用于区域内水位平衡调节");
        strategy3.setSimulating(false);
        strategy3.setActions(Arrays.asList(
            new GateAction(1, "开启"),
            new GateAction(2, "关闭"),
            new GateAction(3, "开启"),
            new GateAction(4, "关闭"),
            new GateAction(5, "开启"),
            new GateAction(6, "关闭")
        ));
        
        // 策略4: 全部开启
        Strategy strategy4 = new Strategy();
        strategy4.setId(4);
        strategy4.setTitle("策略四：全部开启");
        strategy4.setType("应急");
        strategy4.setPriority("高");
        strategy4.setStatus("紧急");
        strategy4.setDescription("所有水闸全部开启，用于紧急情况下的泄洪");
        strategy4.setSimulating(false);
        strategy4.setActions(Arrays.asList(
            new GateAction(1, "开启"),
            new GateAction(2, "开启"),
            new GateAction(3, "开启"),
            new GateAction(4, "开启"),
            new GateAction(5, "开启"),
            new GateAction(6, "开启")
        ));
        
        // 策略5: 全部关闭
        Strategy strategy5 = new Strategy();
        strategy5.setId(5);
        strategy5.setTitle("策略五：全部关闭");
        strategy5.setType("保水");
        strategy5.setPriority("中");
        strategy5.setStatus("备选");
        strategy5.setDescription("所有水闸全部关闭，用于保水或防洪应急处理");
        strategy5.setSimulating(false);
        strategy5.setActions(Arrays.asList(
            new GateAction(1, "关闭"),
            new GateAction(2, "关闭"),
            new GateAction(3, "关闭"),
            new GateAction(4, "关闭"),
            new GateAction(5, "关闭"),
            new GateAction(6, "关闭")
        ));
        
        strategies.add(strategy1);
        strategies.add(strategy2);
        strategies.add(strategy3);
        strategies.add(strategy4);
        strategies.add(strategy5);
        
        return strategies;
    }
    
    private Prediction createMockPrediction(Integer strategyId) {
        Prediction prediction = new Prediction();
        prediction.setWaterLevel(new HashMap<>());
        prediction.setFlowRate(new HashMap<>());
        
        switch (strategyId) {
            case 1: // 上游泄洪
                prediction.getWaterLevel().put("上游", -0.8);
                prediction.getWaterLevel().put("中游", -0.3);
                prediction.getWaterLevel().put("下游", +0.2);
                
                prediction.getFlowRate().put("主河道", +0.7);
                prediction.getFlowRate().put("支流1", +0.4);
                prediction.getFlowRate().put("支流2", +0.2);
                
                prediction.setRiskLevel("低");
                prediction.setTimeEstimate("2小时");
                prediction.setEvaluation("上游水位显著下降，但下游水位略有上升，总体风险可控");
                break;

            case 2: // 下游引水
                prediction.getWaterLevel().put("上游", +0.2);
                prediction.getWaterLevel().put("中游", -0.1);
                prediction.getWaterLevel().put("下游", -0.5);
                
                prediction.getFlowRate().put("主河道", -0.5);
                prediction.getFlowRate().put("支流1", -0.2);
                prediction.getFlowRate().put("支流2", -0.4);
                
                prediction.setRiskLevel("中");
                prediction.setTimeEstimate("3小时");
                prediction.setEvaluation("下游水位明显下降，但上游水位略有上升，需关注上游可能的水位变化");
                break;
                
            case 3: // 平衡调节
                prediction.getWaterLevel().put("上游", -0.3);
                prediction.getWaterLevel().put("中游", -0.1);
                prediction.getWaterLevel().put("下游", -0.2);
                
                prediction.getFlowRate().put("主河道", +0.3);
                prediction.getFlowRate().put("支流1", +0.1);
                prediction.getFlowRate().put("支流2", +0.2);
                
                prediction.setRiskLevel("低");
                prediction.setTimeEstimate("4小时");
                prediction.setEvaluation("各区域水位均有小幅下降，水流平稳，适合长期调节");
                break;
                
            case 4: // 全部开启
                prediction.getWaterLevel().put("上游", -1.2);
                prediction.getWaterLevel().put("中游", -0.8);
                prediction.getWaterLevel().put("下游", -0.5);
                
                prediction.getFlowRate().put("主河道", +1.5);
                prediction.getFlowRate().put("支流1", +0.8);
                prediction.getFlowRate().put("支流2", +0.9);
                
                prediction.setRiskLevel("高");
                prediction.setTimeEstimate("1小时");
                prediction.setEvaluation("所有区域水位迅速下降，但流速过快可能导致河床冲刷和下游水量激增");
                break;
                
            case 5: // 全部关闭
                prediction.getWaterLevel().put("上游", +0.8);
                prediction.getWaterLevel().put("中游", +0.4);
                prediction.getWaterLevel().put("下游", +0.2);
                
                prediction.getFlowRate().put("主河道", -1.2);
                prediction.getFlowRate().put("支流1", -0.6);
                prediction.getFlowRate().put("支流2", -0.5);
                
                prediction.setRiskLevel("极高");
                prediction.setTimeEstimate("5小时");
                prediction.setEvaluation("所有区域水位升高，尤其上游水位上升显著，若降雨持续可能导致溢堤风险");
                break;
                
            default:
                prediction.setRiskLevel("未知");
                prediction.setTimeEstimate("未知");
                prediction.setEvaluation("未知策略");    
            
        }
        
        return prediction;
    }
}
