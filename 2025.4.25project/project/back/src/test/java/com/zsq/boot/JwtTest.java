package com.zsq.boot;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    @Test
    public void testGen() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",1);
        claims.put("username","张三");
        //生成jwt代码
        String token = JWT.create()//静态工厂+建造者模式
            .withClaim("user",claims)
            .withExpiresAt(new Date(System.currentTimeMillis() + 1000*60*60*12))
            .sign(Algorithm.HMAC256("zsq"));//加密算法
        System.out.println(token);
    }
    @Test
    public void testParse() {
        //定义字符串，模拟用户传递过来的token
        String token ="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
            ".eyJ1c2VyIjp7ImlkIjoxLCJ1c2VybmFtZSI6IuW8oOS4iSJ9LCJleHAiOjE3NDQ3NjExNjd9" +
            ".p1pldvTcTuKoapNryGGXM9aQjLNBYpfCpE8imdAv_n0";
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("zsq")).build();//解密算法，和加密算法一致
        DecodedJWT verify = jwtVerifier.verify(token);//验证token，生成一个解析后的jwt对象
        Map<String, Claim> claims = verify.getClaims();
        System.out.println(claims.get("user"));

    }
}
