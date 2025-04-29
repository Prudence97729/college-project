package com.zsq.boot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsq.boot.common.Result;
import com.zsq.boot.entity.Admin;
import com.zsq.boot.entity.User;
import com.zsq.boot.entity.LoginLog;
import com.zsq.boot.entity.Gate;
import com.zsq.boot.service.AdminService;
import com.zsq.boot.service.UserService;
import com.zsq.boot.service.LoginLogService;
import com.zsq.boot.service.GateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LoginLogService loginLogService;
    
    @Autowired
    private GateService gateService;
    
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        try {
            Admin admin = adminService.login(params.get("username"), params.get("password"));
            return Result.success("登录成功", admin);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            Admin admin = adminService.getAdminById(1L);
            if (admin != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", admin);
            } else {
                result.put("code", 404);
                result.put("message", "管理员不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Admin admin) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.updateProfile(admin);
            result.put("code", 200);
            result.put("message", "更新成功");
            result.put("data", adminService.getAdminById(admin.getId()));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    @PutMapping("/password")
    public Result updatePassword(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            String oldPassword = params.get("oldPassword").toString();
            String newPassword = params.get("newPassword").toString();
            
            adminService.updatePassword(id, oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result getUserList() {
        try {
            log.info("获取用户列表");
            
            // 直接获取所有用户，不分页
            List<User> users = userService.list();
            
            return Result.success("获取用户列表成功", users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取登录日志
     */
    @GetMapping("/logs")
    public Result getLoginLogs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "false") Boolean simplified) {
        
        log.info("Received simplified parameter: {}", simplified);
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<LoginLog> queryWrapper = new LambdaQueryWrapper<>();
            
            // 状态过滤
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(LoginLog::getStatus, status);
            }
            
            // 关键词过滤
            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> 
                    wrapper.like(LoginLog::getUsername, keyword)
                           .or()
                           .like(LoginLog::getIp, keyword)
                );
            }
            
            // 日期范围过滤
            if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
                queryWrapper.between(LoginLog::getLoginTime, startDate + " 00:00:00", endDate + " 23:59:59");
            }
            
            // 按时间倒序排序
            queryWrapper.orderByDesc(LoginLog::getLoginTime);
            
            // 获取所有记录
            List<LoginLog> logList = loginLogService.list(queryWrapper);
            
            // 如果是简化模式，转换为活动格式
            if (simplified) {
                List<Map<String, Object>> activities = new ArrayList<>();
                for (LoginLog log : logList) {
                    Map<String, Object> activity = new HashMap<>();
                    // 格式化时间为 "yyyy-MM-dd HH:mm:ss" 格式
                    activity.put("time", log.getLoginTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    activity.put("user", log.getUsername());
                    activity.put("action", log.getStatus().equals("success") ? "登录系统" : "登录失败");
                    activity.put("ip", log.getIp());
                    activities.add(activity);
                }
                return Result.success("获取成功", activities);
            } else {
                return Result.success("获取成功", logList);
            }
        } catch (Exception e) {
            log.error("获取登录日志失败", e);
            return Result.error("获取登录日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统统计数据
     */
    @GetMapping("/statistics")
    public Result getStatistics() {
        try {
            log.info("开始获取系统统计数据");
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取用户总数
            long userCount = userService.count();
            
            // 获取水闸总数
            long gateCount = 0;
            try {
                // 尝试从 GateService 获取水闸列表
                List<Gate> gates = gateService.list();
                gateCount = gates.size();
            } catch (Exception e) {
                log.error("获取水闸数量失败", e);
            }
            
            // 获取今日访问量
            LocalDate today = LocalDate.now();
            String todayStart = today.toString() + " 00:00:00";
            String todayEnd = today.toString() + " 23:59:59";
            
            LambdaQueryWrapper<LoginLog> logWrapper = new LambdaQueryWrapper<>();
            logWrapper.between(LoginLog::getLoginTime, todayStart, todayEnd);
            long todayVisits = loginLogService.count(logWrapper);
            
            // 计算系统运行天数
            // 假设系统上线日期是2023年1月1日
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            long runningDays = ChronoUnit.DAYS.between(startDate, today) + 1;
            
            statistics.put("userCount", userCount);
            statistics.put("gateCount", gateCount);
            statistics.put("todayVisits", todayVisits);
            statistics.put("runningDays", runningDays);
            
            log.info("获取系统统计数据成功: {}", statistics);
            return Result.success("获取系统统计数据成功", statistics);
        } catch (Exception e) {
            log.error("获取系统统计数据失败", e);
            return Result.error("获取系统统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            log.info("更新用户信息: id={}", id);
            
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 更新昵称
            if (userData.containsKey("nickname")) {
                user.setNickname((String) userData.get("nickname"));
            }
            
            // 更新邮箱
            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
            }
            
            // 更新密码 - 暂时不加密
            if (userData.containsKey("password")) {
                String password = (String) userData.get("password");
                user.setPassword(password); // 直接设置明文密码
            }
            
            boolean success = userService.updateById(user);
            
            if (success) {
                return Result.success("更新用户成功");
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新用户失败: " + e.getMessage());
        }
    }
} 