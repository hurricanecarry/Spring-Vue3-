package com.yunpan.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer{
     @Override
     public void addCorsMappings(CorsRegistry registry){
         registry.addMapping("/**")                    // 所有接口
                .allowedOriginPatterns("*")           // 允许所有来源
                .allowedMethods("*")                  // 允许所有方法
                .allowedHeaders("*")                  // 允许所有请求头
                .allowCredentials(true)               // 允许携带 cookie
                .maxAge(3600);                        // 预检请求缓存 1 小时

     }
}
