package com.zsq.boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zsq.boot.common.Result;
import com.zsq.boot.entity.User;
import com.zsq.boot.entity.LoginLog;
import com.zsq.boot.service.UserService;
import com.zsq.boot.service.LoginLogService;
import com.zsq.boot.utils.JwtUtil;
import com.zsq.boot.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
@CrossOrigin  // 允许跨域请求
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;


    @Autowired
    private HttpServletRequest request;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.register(params.get("username"), params.get("password"));
            result.put("code", 200);
            result.put("message", "注册成功");
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginForm, HttpServletRequest request) {
        try {
            String username = loginForm.get("username");
            String password = loginForm.get("password");
            
            // 验证用户名和密码
            User user = userService.login(username, password);//会返回user对象
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("username", user.getUsername());
            String token = JwtUtil.genToken(claims);
            Map<String, Object> data = new HashMap<>();//存储token和user信息
            data.put("token", token);
            data.put("user", user);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    // 获取用户信息
    @GetMapping("/info")
    public Map<String, Object> getUserInfo() {
        System.out.println("---------------头部信息-----------");
        String authToken = request.getHeader("Authorization");
        System.out.println("Authorization Token: " + authToken);
        //System.out.println(UserContext.getCurrentUser().getUsername());
        Map<String, Object> result = new HashMap<>();
        try {
            String userStr = request.getHeader("user");
            log.info("Received user header: {}", userStr);
            
            if (userStr != null) {
                userStr = java.net.URLDecoder.decode(userStr, "UTF-8");
                JSONObject userJson = new JSONObject(userStr);
                Long userId = userJson.getLong("id");
                log.info("Parsed user ID: {}", userId);
                
                User user = userService.getUserInfo(userId);
                log.info("Final user info to return: {}", user);
                
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", user);
            } else {
                throw new RuntimeException("未找到用户信息");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 更新用户信息
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求头中获取用户ID
            String userStr = request.getHeader("user");
            if (userStr != null) {
                userStr = java.net.URLDecoder.decode(userStr, "UTF-8");
                JSONObject userJson = new JSONObject(userStr);
                Long userId = userJson.getLong("id");
                
                // 设置正确的用户ID
                user.setId(userId);
                
                // 添加日志
                log.info("收到更新请求：{}", user);
                
                userService.updateProfile(user);
                
                // 获取更新后的用户信息
                User updatedUser = userService.getUserInfo(userId);
                
                result.put("code", 200);
                result.put("message", "更新成功");
                result.put("data", updatedUser);
            } else {
                throw new RuntimeException("未找到用户信息");
            }
        } catch (Exception e) {
            log.error("更新失败：", e);
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 修改密码
    @PutMapping("/password")
    public Map<String, Object> updatePassword(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求头中获取用户信息
            String userStr = request.getHeader("user");
            if (userStr != null) {
                userStr = java.net.URLDecoder.decode(userStr, "UTF-8");
                JSONObject userJson = new JSONObject(userStr);
                Long userId = userJson.getLong("id");
                
                userService.updatePassword(
                    userId,
                    params.get("oldPassword"),
                    params.get("newPassword")
                );
                
                result.put("code", 200);
                result.put("message", "密码修改成功");
            } else {
                throw new RuntimeException("未找到用户信息");
            }
        } catch (Exception e) {
            log.error("修改密码失败：", e);
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/avatar")
    public Map<String, Object> updateAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 从请求头中获取用户信息
            String userStr = request.getHeader("user");
            if (userStr != null) {
                // 2. 解码并解析用户信息
                userStr = java.net.URLDecoder.decode(userStr, "UTF-8");
                JSONObject userJson = new JSONObject(userStr);
                Long userId = userJson.getLong("id");
                
                // 3. 调用service更新头像
                String avatarUrl = userService.updateAvatar(userId, file);
                
                // 4. 获取更新后的完整用户信息
                User updatedUser = userService.getUserInfo(userId);
                
                // 5. 返回成功结果
                result.put("code", 200);
                result.put("message", "上传成功");
                result.put("data", updatedUser);  // 返回完整的用户信息
            } else {
                throw new RuntimeException("未找到用户信息");
            }
        } catch (Exception e) {
            // 6. 如果出错，记录日志并返回错误信息
            log.error("上传头像失败：", e);
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    
}
