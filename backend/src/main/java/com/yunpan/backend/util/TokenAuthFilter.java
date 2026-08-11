package com.yunpan.backend.util;

import com.yunpan.backend.config.JwtConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class TokenAuthFilter extends OncePerRequestFilter {
        private final TokenProvider tokenProvider;
        private final JwtConfig jwtConfig;

    @Override
    public void doFilterInternal(HttpServletRequest request,HttpServletResponse response,
         FilterChain filterChain)throws ServletException,IOException{
             String token=getToken(request);
             if(token!=null){
                 if(tokenProvider.vaildateToken(token)){
                     String userId=tokenProvider.getUserIdFromToken(token);
                     UsernamePasswordAuthenticationToken  authentication=
                           new UsernamePasswordAuthenticationToken(userId, null,Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                 } else {
                     // token 过期或无效 → 返回 401，前端拦截器自动跳登录
                     response.setStatus(401);
                     response.setContentType("application/json;charset=UTF-8");
                     response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
                     return;
                 }
             }
            filterChain.doFilter(request, response);
         }

   public String getToken(HttpServletRequest req){
       String auth=req.getHeader(jwtConfig.getHeader());
       if(auth!=null&&auth.startsWith(jwtConfig.getTokenPrefix())){
             String token=auth.substring(jwtConfig.getTokenPrefix().length()).trim();
              return  token;
       }
       return null;
   }
}
