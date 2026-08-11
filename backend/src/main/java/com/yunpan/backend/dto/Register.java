package com.yunpan.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Register {
    String password;
    String  nickname;
    String email;
}
