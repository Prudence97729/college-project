package com.zsq.boot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsq.boot.entity.Gate;
import com.zsq.boot.mapper.GateMapper;
import com.zsq.boot.service.GateService;
import org.springframework.stereotype.Service;

@Service
public class GateServiceImpl extends ServiceImpl<GateMapper, Gate> implements GateService {
} 