package com.yunpan.backend.dto;

import com.yunpan.backend.entity.UserInfo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRes {
    UserInfo userInfo;
    String   token;
}
