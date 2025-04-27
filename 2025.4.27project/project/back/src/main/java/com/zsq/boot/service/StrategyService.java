package com.zsq.boot.service;

import java.util.List;
import java.util.Map;

import com.zsq.boot.entity.Strategy;
import com.zsq.boot.entity.StrategyGenDTO;

public interface StrategyService {
    List<Strategy> generateStrategies(StrategyGenDTO dto);
    Map<String, Object> simulateStrategy(Map<String, Object> strategy);
} 