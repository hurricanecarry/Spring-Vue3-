package com.yunpan.backend.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yunpan.backend.common.Result;
import com.yunpan.backend.config.FileConfig;
import com.yunpan.backend.dto.Login;
import com.yunpan.backend.dto.LoginRes;
import com.yunpan.backend.dto.Register;
import com.yunpan.backend.dto.RegisterRes;
import com.yunpan.backend.entity.UserInfo;
import com.yunpan.backend.service.impl.UserInfoServiceImpl;
import com.yunpan.backend.util.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;




/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hurricanecarry
 * @since 2026-06-13
 */
//本身就是bean
@RestController
@RequestMapping("/backend/auth")
public class UserInfoController {

@Autowired
private  PasswordEncoder passwordEncoder;
@Autowired
private UserInfoServiceImpl usertable;
@Autowired
private TokenProvider tokenProvider;
@Autowired
private FileInfoController fileInfoController;
	@Autowired
	private FileConfig fileConfig;



@PostMapping("/login")
public Result<?> login(@RequestParam("account")String account,@RequestParam("password")String password) {
    try {
     UserInfo userInfo=usertable.getOne(new QueryWrapper<UserInfo>().eq("email",account)
         .or().eq("nick_name",account));
     if(userInfo==null) return Result.fail(404,"用户不存在");
     if(!passwordEncoder.matches(password, userInfo.getPassword())) return Result.fail(403,"密码错误");
     else{
         String token=tokenProvider.createToken(userInfo.getUserId(),userInfo.getNickname());
         LoginRes loginRes=new LoginRes(token, userInfo);
         return Result.success("认证成功", loginRes);
     }
     } catch (Exception e) {
        return Result.fail(500,"登录失败: " + e.getMessage());
     }
     
} 

@PostMapping("/register")
public Result<?> register(@RequestParam("email")String email,@RequestParam("nickName")String nickName
     ,@RequestParam("password")String password) 
{  
   try {
    UserInfo exist=usertable.getOne(new QueryWrapper<UserInfo>().eq("email",email).or().eq("nick_name", nickName));
    if(exist!=null)  return Result.fail(409, "用户名/邮箱重复");
       UserInfo userInfo=new UserInfo();
       userInfo.setUserId(String.valueOf(IdWorker.getId()));
       userInfo.setEmail(email);
       userInfo.setNickname(nickName);
       userInfo.setPassword(passwordEncoder.encode(password));
       userInfo.setRegisterDate(LocalDateTime.now());
       userInfo.setStatus((byte)1);
       userInfo.setTotalSpace(2L*1024*1024*1024);
       userInfo.setUsedSpace(0L);
       usertable.save(userInfo);
       String token=tokenProvider.createToken(userInfo.getUserId(), nickName);
       LoginRes loginRes=new LoginRes(token, userInfo);
       return Result.success("用户创建成功", loginRes);
   } catch (Exception e) {
       return Result.fail(500,"创建失败: " + e.getMessage());
   }
}

//返回头像磁盘地址----会有坑，可能目录里面已经有相同文件
@PostMapping("/setting/avatar/change")
public Result<?> AvatarChange(@RequestParam("avatarFile")MultipartFile file) {
   String userId=fileInfoController.getIdFromToken();
   try{
     String ext=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
     String fileName="avatar"+ext;
     Path settingDir=Paths.get(fileConfig.getTotalPath(),userId,"setting");
     Files.createDirectories(settingDir);
     Path targetPath=settingDir.resolve(fileName);
     file.transferTo(targetPath);
     UserInfo user=usertable.getById(userId);
     user.setAvatar(targetPath.toString());
     usertable.updateById(user);
     return Result.success("头像上传成功",targetPath.toString());
   }catch(Exception e){
     return Result.fail(500,"上传失败:"+e.getMessage());
   }
}

@GetMapping("/setting/avatar/get")
public void getAvatar(HttpServletResponse res,@RequestParam("userId")String userId) {
    try{
      UserInfo userInfo=usertable.getById(userId);
      if(userInfo==null||userInfo.getAvatar()==null){
         res.setStatus(404);
         return;
      }
      Path p=Paths.get(userInfo.getAvatar());
      if(!Files.exists(p)){
         res.setStatus(404);
         return;
      }
      String avatarPath=userInfo.getAvatar();
      String ext=avatarPath.substring(avatarPath.lastIndexOf("."));
      res.setContentType("image/"+ext.substring(1));
      Files.copy(p,res.getOutputStream());
    }catch(Exception e){
        res.setStatus(500);
    }
}



@PostMapping("/setting/basic/change")
public Result<?> basicChange(@RequestParam("nickName")String nickName,
   @RequestParam("email")String email,@RequestParam("password")String password) {
   String userId = fileInfoController.getIdFromToken();
    UserInfo user = usertable.getById(userId);
    user.setNickname(nickName);
    user.setEmail(email);
    if (password != null && !password.isEmpty()) {
        user.setPassword(passwordEncoder.encode(password));
    }
    usertable.updateById(user);
    return Result.success("修改成功", null);
}




}
