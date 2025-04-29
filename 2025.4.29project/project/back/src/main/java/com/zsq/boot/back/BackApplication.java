package com.zsq.boot.back;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.zsq.boot"})
@MapperScan("com.zsq.boot.mapper")
public class BackApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
		log.info("=== 系统启动成功！===");
	}
}
