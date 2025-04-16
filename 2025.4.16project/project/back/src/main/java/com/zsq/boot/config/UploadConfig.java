package com.zsq.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.File;

@Component
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {
    private String uploadPath;

    public String getUploadPath() {
        // 规范化路径
        if (uploadPath != null) {
            uploadPath = uploadPath.replace('\\', '/');
            if (!uploadPath.endsWith("/")) {
                uploadPath = uploadPath + "/";
            }
        }
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    @PostConstruct
    public void init() {
        File dir = new File(uploadPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create upload directory: " + uploadPath);
        }
        if (!dir.canRead() || !dir.canWrite()) {
            throw new RuntimeException("Upload directory is not accessible: " + uploadPath);
        }
    }
} 