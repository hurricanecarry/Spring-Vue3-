package com.yunpan.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T>{
  private int code;
  private String message;
  private T data;
  

public static<T>  Result<T> success (String msg,T data){
       return new Result<T>(200,msg, data);
}

public static<T> Result<T> fail(int code,String message){
     return new Result<>(code,message,null);
}

public static<T> Result<T> fail(String msg){
    return fail(500, msg);
}


}
