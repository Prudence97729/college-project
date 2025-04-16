package com.zsq.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsq.boot.entity.User;
import com.zsq.boot.mapper.UserMapper;
import com.zsq.boot.service.UserService;
import com.zsq.boot.config.UploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UploadConfig uploadConfig;
    
    private static final String DEFAULT_AVATAR = "/api/upload/default.png";
    
    
    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);//返回user完整字段
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 直接比较密码，不进行加密
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }
    
    @Override
    public User register(String username, String password) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        Long count = userMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public void updateProfile(User user) {
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 验证旧密码
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        // 更新新密码
        user.setPassword(newPassword);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) throws IOException {
        log.info("Upload path configured as: {}", uploadConfig.getUploadPath());
        
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;//生成全球唯一文件名
        
        String uploadPath = uploadConfig.getUploadPath();
        File dir = new File(uploadPath);
        
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建上传目录: " + uploadPath);
        }
        
        if (!dir.canWrite()) {
            throw new RuntimeException("上传目录没有写入权限: " + uploadPath);
        }
        
        File destFile = new File(dir, fileName);
        log.info("Saving file to: {}", destFile.getAbsolutePath());
        
        Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        if (!destFile.exists() || !destFile.canRead()) {
            throw new RuntimeException("文件保存失败或无法访问: " + destFile.getAbsolutePath());
        }
        
        String avatarUrl = "/api/upload/" + fileName;
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUserPic(avatarUrl);
        updateUser.setUpdateTime(LocalDateTime.now());
        
        int rows = userMapper.updateById(updateUser);
        if (rows != 1) {
            if (!destFile.delete()) {
                log.warn("Failed to delete file after database update failure: {}", destFile.getAbsolutePath());
            }
            throw new RuntimeException("更新失败");
        }
        
        return avatarUrl;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(null);
        
        if (user.getUserPic() == null || user.getUserPic().isEmpty()) {
            user.setUserPic(DEFAULT_AVATAR);
        }
        
        return user;
    }

} 
