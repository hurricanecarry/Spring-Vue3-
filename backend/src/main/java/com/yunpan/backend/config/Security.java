package com.yunpan.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.yunpan.backend.util.TokenAuthFilter;

//全局单例
@Configuration
public class Security {
    private final TokenAuthFilter tokenAuthFilter;
    public Security(TokenAuthFilter tokenAuthFilter){
         this.tokenAuthFilter=tokenAuthFilter;
    }

    @Bean
    public SecurityFilterChain mychain(HttpSecurity http)throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth->auth.requestMatchers("/backend/auth/login","/backend/auth/register").permitAll()
                 .anyRequest().permitAll())
        .addFilterBefore(tokenAuthFilter,UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    //全局工具bean
    @Bean
    public PasswordEncoder passwordEncoder(){
        //密码加密器
        return new BCryptPasswordEncoder();
    }
}
