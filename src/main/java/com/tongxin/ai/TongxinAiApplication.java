package com.tongxin.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.tongxin.ai.mapper")
@SpringBootApplication
public class TongxinAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TongxinAiApplication.class, args);
    }

}
