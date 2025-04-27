package com.zsq.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsq.boot.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface UserService extends IService<User> {
    User login(String username, String password);
    
    User register(String username, String password);
    
    void updateProfile(User user);
    
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    String updateAvatar(Long userId, MultipartFile file) throws IOException;
    
    User getUserInfo(Long userId);
    
} 