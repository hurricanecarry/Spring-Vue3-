package com.yunpan.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yunpan.backend.common.Result;
import com.yunpan.backend.entity.FileInfo;
import com.yunpan.backend.entity.ShareInfo;
import com.yunpan.backend.service.impl.FileInfoServiceImpl;
import com.yunpan.backend.service.impl.ShareInfoServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hurricanecarry
 * @since 2026-06-13
 */
@RestController
@RequestMapping("/backend/share")
public class ShareInfoController {
	@Autowired
	private  FileInfoServiceImpl filetable;
	@Autowired
	private ShareInfoServiceImpl sharetable;
	@Autowired
	private FileInfoController fileInfoController;
	@Autowired
	private com.yunpan.backend.service.impl.UserInfoServiceImpl usertable;
	@Autowired
	private com.yunpan.backend.util.TokenProvider tokenProvider;


	@PostMapping("/create")
	public Result<?> createShare(@RequestParam("fileId")String fileId,
	             @RequestParam("shareType")Byte shareType,@RequestParam(name="shareCode",required = false)String shareCode,
	            @RequestParam("expire_day")Integer expire_day) {
	           FileInfo fileInfo=filetable.getById(fileId);
	           String shareId=String.valueOf(IdWorker.getId()).substring(0,10);
	           String userId=fileInfoController.getIdFromToken();
	           if(shareType==0){
	             shareCode=null;
	           }
	           LocalDateTime expireTime=expire_day==null?null:LocalDateTime.now().plusDays(expire_day);

	           if(!fileInfo.getUserId().equals(userId)){
	             return Result.fail(403, "Id不匹配");
	           }
	           try{
	              ShareInfo shareInfo=new ShareInfo();
	           shareInfo.setCreatedTime(LocalDateTime.now());
	           shareInfo.setFileId(fileId);
	           shareInfo.setExpireTime(expireTime);
	           shareInfo.setShareCode(shareCode);
           shareInfo.setShareType(shareType);
           shareInfo.setFileName(fileInfo.getFileName());
	           shareInfo.setShareId(shareId);
	           shareInfo.setStatus((byte)0);
           shareInfo.setViewCount(0);
           shareInfo.setDownloadCount(0);
	           shareInfo.setUserId(fileInfo.getUserId());
	           sharetable.save(shareInfo);
	           return Result.success("分享创建成功", shareId);
	           }catch(Exception e){
	               return Result.fail("分享创建失败"+e.getMessage());
	           }
	}

   // 验证提取码并创建分享会话 token返回（30分钟有效）
   @PostMapping("/verify")
  public Result<?> verifyCode(@RequestParam("shareId") String shareId,
		        @RequestParam("shareCode") String shareCode) {
		    ShareInfo share = sharetable.getById(shareId);
		    if (share == null || share.getStatus() != 0) return Result.fail(404, "分享不存在");
		    if (share.getShareType() != null && share.getShareType() == 0) return Result.fail(400, "公开分享无需验证");
		    if (!shareCode.equals(share.getShareCode())) return Result.fail(403, "提取码错误");
		    ShareInfo shareInfo=sharetable.getById(shareId);
			FileInfo rootFile=filetable.getById(shareInfo.getFileId());
		    String sessionToken = tokenProvider.createShareToken(shareId);
			Map<String,Object> res=new HashMap<>();
			res.put("shareToken", sessionToken);
			res.put("rootFile", rootFile);
		    return Result.success("验证成功", res);
	}

    //展示文件夹/文件+
	//需要shareId,[shareCode],[parentId] -- parentId非空时列出该文件夹子文件
	@PostMapping("/showshare")
	public Result<?> showShare(@RequestParam("shareId")String shareId,
		    @RequestParam(name="shareToken",required=false)String shareToken,
	    @RequestParam(name="shareCode",required=false)String shareCode,
	    @RequestParam(name="parentId",required=false)String parentId) {
	     // ① 校验分享
	     ShareInfo shareInfo=sharetable.getById(shareId);
	     if (shareInfo == null || shareInfo.getStatus() != 0) {
	        return Result.fail(404, "分享不存在或已失效");
	     }
	     // ② 提取码校验
	     if(shareInfo.getShareType()!=null && shareInfo.getShareType()==1){
		         boolean tokenOk = shareToken!=null && tokenProvider.vaildateToken(shareToken);
		         boolean codeOk = shareCode!=null && shareCode.equals(shareInfo.getShareCode());
				 //都失败才报错
		         if(!tokenOk && !codeOk) return Result.fail(403,"提取错误");
	     }
	     // ③ 访问计数（仅首次进入分享时+1）
	     if(parentId==null||parentId.isEmpty()){
	         shareInfo.setViewCount((shareInfo.getViewCount()==null?0:shareInfo.getViewCount())+1);
	         sharetable.updateById(shareInfo);
	     }
	     // ④ 根文件信息
	     FileInfo rootFile=filetable.getById(shareInfo.getFileId());
	     // ⑤ 确定要列出的文件夹：有parentId则列该子目录，否则列根目录
	     String currentFolderId=(parentId!=null&&!parentId.isEmpty())?parentId:shareInfo.getFileId();
	     FileInfo currentFolder=(parentId!=null&&!parentId.isEmpty())?filetable.getById(parentId):rootFile;
	     // ⑥ 如果目标是文件夹，查子文件
	     List<FileInfo> children=null;
	     if(currentFolder!=null&&currentFolder.getFileType()==0){
	         children=filetable.list(new QueryWrapper<FileInfo>()
	             .eq("parent_id",currentFolderId)
	             .eq("status",0)
	             .orderByAsc("file_type")
	             .orderByAsc("file_name"));
	     }
	     // ⑦ 组装返回
	     Map<String,Object> result=new HashMap<>();
	     result.put("shareId",shareInfo.getShareId());
         result.put("viewCount",shareInfo.getViewCount());
         result.put("downloadCount",shareInfo.getDownloadCount());
	     result.put("shareType",shareInfo.getShareType());
	     result.put("expireTime",shareInfo.getExpireTime());
	     result.put("rootFile",rootFile);
	     result.put("currentFolder",currentFolder);
         com.yunpan.backend.entity.UserInfo owner = usertable.getById(shareInfo.getUserId());
	         result.put("ownerName", owner != null ? owner.getNickname() : "未知");
         result.put("ownerUserId", shareInfo.getUserId());
	     result.put("children",children);
	     return Result.success("获取成功",result);
	}


	@PostMapping("/saveshare")
	public Result<?> saveShare(@RequestParam("fileId")String fileId,@RequestParam("parentId")String parentId
	    ,@RequestParam("shareId")String shareId) {
	   ShareInfo shareInfo=sharetable.getById(shareId);
	   String userId=fileInfoController.getIdFromToken();
	   FileInfo fileInfo=filetable.getById(fileId);
	   if (fileInfo == null) return Result.fail(404, "文件不存在");
	   String md5=fileInfo.getMd5();
	   String fileName=fileInfo.getFileName();
	   try{
	     long fs = fileInfo.getFileSize() != null ? fileInfo.getFileSize() : 0L;
             fileInfoController.saveFileRecord(userId,parentId,fileName,fileInfo.getFileType(),fileInfo.getFilePath(),md5,fs);
	     List<FileInfo> files=new ArrayList<FileInfo>();
	     fileInfoController.dfsFolder(fileId,fileInfo.getUserId(),(byte)0, files);
	     for(var file:files){
	         if(file.getFileType()==0) fileInfoController.saveFileRecord(userId, parentId, file.getFileName(),(byte)0, null, null, 0);
	         else fileInfoController.saveFileRecord(userId, parentId, file.getFileName(),(byte)1, file.getFilePath(), file.getMd5(), file.getFileSize());
	     }
	     shareInfo.setDownloadCount((shareInfo.getDownloadCount()==null?0:shareInfo.getDownloadCount())+1);
	     sharetable.updateById(shareInfo);
	     return Result.success("文件保存成功",null);
	   }catch(Exception e){
	       return Result.fail(500,"文件保存失败"+e.getMessage());
	   }

	}
   
	//对分享自己，获取分享列表
	@PostMapping("/listshare")
	public Result<?> listShare() {
	   String userId=fileInfoController.getIdFromToken();
	   List<ShareInfo> sharelist=new ArrayList<ShareInfo>();
	   QueryWrapper<ShareInfo> q=new QueryWrapper<ShareInfo>().eq("user_id",userId);
	   try {
	      sharelist=sharetable.list(q);
	       return Result.success("共享列表获取成功", sharelist);
	   } catch (Exception e) {
	      return Result.fail(500,"共享列表获取失败"+e.getMessage());
	   }

	}
    //取消分享
	@PostMapping("/cancel")
	public Result<?> cancelShare(@RequestParam("shareId")String shareId) {
	     ShareInfo shareInfo=sharetable.getById(shareId);
        if(!shareInfo.getUserId().equals(fileInfoController.getIdFromToken())){
          return  Result.fail(403, "ID不匹配");
        }
	     try {
	      shareInfo.setStatus((byte)1);
	     sharetable.updateById(shareInfo);
	     return Result.success("取消成功", null);
	     } catch (Exception e) {
	       return   Result.fail(500, "取消失败");
	     }
	}


}
