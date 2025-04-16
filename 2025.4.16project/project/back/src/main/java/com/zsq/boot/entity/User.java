package com.zsq.boot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data                   // Lombok注解，自动生成getter、setter等方法
@TableName("user")      // 指定对应的数据库表名
public class User {
    @TableId(type = IdType.AUTO)    // 指定这是一个自增的主键
    private Long id;                 // 用户ID
    
    @TableField("username")
    private String username;         // 用户名
    
    @TableField("password")
    private String password;         // 密码
    
    @TableField("nickname")
    private String nickname;         // 昵称
    
    @TableField("email")
    private String email;            // 邮箱
    
    @TableField("user_pic")
    private String userPic;          // 用户头像
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime; // 更新时间
} 