package com.yunpan.backend.util;



import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.yunpan.backend.config.JwtConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProvider {
    //只赋值一次，不被改变
    private final SecretKey key;
    private final long expiration;
    private final String tokenPrefix;
    
    public  TokenProvider(JwtConfig config){
           this.key=Keys.hmacShaKeyFor(config.getSecret().getBytes(StandardCharsets.UTF_8));
           this.expiration=config.getExpiration();
           this.tokenPrefix=config.getTokenPrefix();
    }
//token里面有username，userId,创建时间，销毁时间，根据key生成的最终文
    public String  createToken(String userId,String  nickName){
         return Jwts.builder().subject(userId).claim("useranme", nickName)
         .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration))
         .signWith(key).compact();
    }

    public String getUserIdFromToken(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
          .getPayload().getSubject();
    }

    // 分享会话 token（30分钟有效）
    public String createShareToken(String shareId) {
        return Jwts.builder().subject(shareId)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
            .signWith(key).compact();
    }

    public String getShareIdFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            .getPayload().getSubject();
    }

    public boolean vaildateToken(String token){
          try{
              Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
              return true;
          }catch(Exception e){
             e.printStackTrace();
             return false;
          }
    }
}
