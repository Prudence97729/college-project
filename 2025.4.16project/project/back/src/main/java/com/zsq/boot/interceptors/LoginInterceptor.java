package com.zsq.boot.interceptors;

import com.zsq.boot.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token = request.getHeader("Authorization");
        //验证token
        try{
            Map<String,Object> claims = JwtUtil.parseToken(token);//如果没有解析成功，会JwtUtil抛出异常
            //放行
            return true;
        } catch (Exception e){
            //http响应码为401
            response.setStatus(401);
            //不放行
            return false;
        }
    }
}
