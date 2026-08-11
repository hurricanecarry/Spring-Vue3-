package com.yunpan.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


import lombok.Data;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private Long expiration;
    private String header;
    private String tokenPrefix;

    // 截掉 properties 文件里可能的尾空格
    public void setSecret(String secret) {
        this.secret = secret != null ? secret.trim() : null;
    }
    public void setHeader(String header) {
        this.header = header != null ? header.trim() : null;
    }
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix != null ? tokenPrefix.trim() : null;
    }
}
