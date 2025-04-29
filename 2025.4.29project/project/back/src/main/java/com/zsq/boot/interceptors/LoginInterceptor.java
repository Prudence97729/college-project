package com.zsq.boot.interceptors;

import com.zsq.boot.entity.User;
import com.zsq.boot.utils.JwtUtil;
import com.zsq.boot.utils.UserContext;
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
            User user = new User();
            user.setId((Long) claims.get("id"));
            user.setUsername((String)claims.get("username"));
            //放行
            UserContext.setCurrentUser(user);
            return true;
        } catch (Exception e){
            //http响应码为401
            response.setStatus(401);
            //不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clearCurrentUser();//清理Threadlocal信息，防止内存泄漏
    }
}
