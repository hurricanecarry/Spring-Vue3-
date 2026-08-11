package com.yunpan.backend.common;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public  Result<?> handleExResult(Exception e){
           return  Result.fail(e.getMessage());
    }
}
