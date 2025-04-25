package com.zsq.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsq.boot.entity.Admin;
import com.zsq.boot.entity.User;
import com.zsq.boot.entity.LoginLog;
import com.zsq.boot.mapper.AdminMapper;
import com.zsq.boot.mapper.UserMapper;
import com.zsq.boot.mapper.LoginLogMapper;
import com.zsq.boot.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    
    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    @Override
    public Admin login(String username, String password) {
        Admin admin = lambdaQuery()
                .eq(Admin::getUsername, username)
                .one();
                
        if (admin == null) {
            throw new RuntimeException("管理员账号不存在");
        }
        
        if (!admin.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        
        // 更新最后登录时间
        admin.setUpdateTime(LocalDateTime.now());
        updateById(admin);
        
        // 安全起见，不返回密码
        admin.setPassword(null);
        return admin;
    }
    
    @Override
    public Admin getAdminById(Long id) {
        Admin admin = getById(id);
        if (admin != null) {
            // 安全起见，不返回密码
            admin.setPassword(null);
        }
        return admin;
    }
    
    @Override
    public void updateProfile(Admin admin) {
        // 确保只更新允许的字段
        Admin updateAdmin = new Admin();
        updateAdmin.setId(admin.getId());
        updateAdmin.setNickname(admin.getNickname());
        updateAdmin.setEmail(admin.getEmail());
        updateAdmin.setUserPic(admin.getUserPic());
        updateAdmin.setUpdateTime(LocalDateTime.now());
        
        updateById(updateAdmin);
    }
    
    @Override
    public void updatePassword(Long adminId, String oldPassword, String newPassword) {
        Admin admin = getById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 验证旧密码
        if (!oldPassword.equals(admin.getPassword())) {
            log.warn("原密码不匹配");
            throw new RuntimeException("原密码错误");
        }
        
        // 更新密码
        lambdaUpdate()
            .set(Admin::getPassword, newPassword)
            .set(Admin::getUpdateTime, LocalDateTime.now())
            .eq(Admin::getId, adminId)
            .update();
    }
    
    @Override
    public List<User> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        // 安全起见，清除密码信息
        users.forEach(user -> user.setPassword(null));
        return users;
    }
    
    @Override
    public List<Admin> getAllAdmins() {
        List<Admin> admins = list();
        // 安全起见，清除密码信息
        admins.forEach(admin -> admin.setPassword(null));
        return admins;
    }
    
    @Override
    public Page<LoginLog> getLoginLogs(Integer page, Integer size, String startDate, String endDate) {
        Page<LoginLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<LoginLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加时间范围条件
        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            queryWrapper.between(LoginLog::getLoginTime, start, end);
        }
        
        // 按时间倒序排序
        queryWrapper.orderByDesc(LoginLog::getLoginTime);
        
        return loginLogMapper.selectPage(pageParam, queryWrapper);
    }
} 