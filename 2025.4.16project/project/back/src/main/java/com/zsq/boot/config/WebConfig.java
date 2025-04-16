package com.zsq.boot.config;

import com.zsq.boot.interceptors.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);
    
    @Autowired
    private UploadConfig uploadConfig;

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //登录接口和注册接口不拦截
        //registry.addInterceptor(loginInterceptor).excludePathPatterns("/api/user/login","/api/user/register");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadConfig.getUploadPath();
        path = path.replace('\\', '/');
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        
        log.info("Configuring resource mapping:");
        log.info("Pattern: /api/upload/**");
        log.info("Location: file:{}", path);
        
        registry.addResourceHandler("/api/upload/**")
                .addResourceLocations("file:" + path)
                .setCacheControl(CacheControl.noCache());
        
        // 验证目录
        File uploadDir = new File(path);
        if (!uploadDir.exists()) {
            log.warn("Directory does not exist: {}", path);
            if (!uploadDir.mkdirs()) {
                log.error("Failed to create directory: {}", path);
            }
        }
        
        // 列出文件
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                log.info("Files in directory:");
                for (File file : files) {
                    log.info(" - {} ({} bytes)", file.getName(), file.length());
                }
            }
        }
    }

    // 添加跨域配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")  // 前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
} 
