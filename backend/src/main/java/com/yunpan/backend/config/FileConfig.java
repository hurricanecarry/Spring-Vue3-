package com.yunpan.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix ="file")
public class FileConfig {
    String totalPath;
}
