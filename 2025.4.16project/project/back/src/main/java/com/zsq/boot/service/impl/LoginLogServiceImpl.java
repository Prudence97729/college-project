package com.zsq.boot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsq.boot.entity.LoginLog;
import com.zsq.boot.mapper.LoginLogMapper;
import com.zsq.boot.service.LoginLogService;
import org.springframework.stereotype.Service;

@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {
    // 继承自ServiceImpl，已经实现了IService中的所有方法
} 