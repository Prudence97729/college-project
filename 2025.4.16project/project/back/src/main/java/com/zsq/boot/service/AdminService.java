package com.zsq.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsq.boot.entity.Admin;
import com.zsq.boot.entity.User;
import com.zsq.boot.entity.LoginLog;
import java.util.List;

public interface AdminService extends IService<Admin> {
    // 管理员登录
    Admin login(String username, String password);
    
    // 根据ID获取管理员信息
    Admin getAdminById(Long id);
    
    // 更新管理员资料
    void updateProfile(Admin admin);
    
    // 更新管理员密码
    void updatePassword(Long adminId, String oldPassword, String newPassword);
    
    // 获取所有用户信息
    List<User> getAllUsers();
    
    // 获取所有管理员信息
    List<Admin> getAllAdmins();
    
    // 获取登录日志
    Page<LoginLog> getLoginLogs(Integer page, Integer size, String startDate, String endDate);
} 