package com.zsq.boot.aspect;

import com.zsq.boot.entity.LoginLog;
import com.zsq.boot.mapper.LoginLogMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Aspect
@Component
public class LoginLogAspect {
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    // 处理所有登录情况，包括成功和失败
    @AfterReturning(
        pointcut = "execution(* com.zsq.boot.controller.*.login(..))",
        returning = "result"
    )
    public void afterLogin(JoinPoint joinPoint, Object result) {
        try {
            // 检查返回结果是否为Result类型
            if (result instanceof com.zsq.boot.common.Result) {
                com.zsq.boot.common.Result resultObj = (com.zsq.boot.common.Result) result;
                Integer code = resultObj.getCode();
                boolean isSuccess = (code != null && code == 200);
                
                // 获取请求参数
                Object[] args = joinPoint.getArgs();
                Map<String, String> params = (Map<String, String>) args[0];
                String username = params.get("username");
                
                // 获取请求IP
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String ip = request.getRemoteAddr();
                
                // 记录日志
                LoginLog log = new LoginLog();
                log.setUsername(username);
                log.setIp(ip);
                log.setStatus(isSuccess ? "success" : "failed");
                log.setLoginTime(LocalDateTime.now());
                
                loginLogMapper.insert(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 