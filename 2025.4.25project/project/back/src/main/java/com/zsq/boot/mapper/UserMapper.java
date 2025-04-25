package com.zsq.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsq.boot.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
} 