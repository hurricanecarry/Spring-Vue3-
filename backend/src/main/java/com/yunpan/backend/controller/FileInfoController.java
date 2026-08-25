package com.yunpan.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DrbgParameters.Reseed;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yunpan.backend.common.Result;
import com.yunpan.backend.config.FileConfig;
import com.yunpan.backend.entity.FileInfo;
import com.yunpan.backend.enums.FileCategoryEnum;
import com.yunpan.backend.service.impl.FileInfoServiceImpl;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author hurricanecarry
 * @since 2026-06-13
 */
@RestController
@RequestMapping("/backend/file")
public class FileInfoController {
   @Autowired
   private FileInfoServiceImpl filetable;
   @Autowired
   private FileConfig fileConfig;

   public String getIdFromToken() {
      return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
   }

   private String toTrueName(String parentId, String userId, String fileName, Byte type) {
      String newName = fileName;
      int count = 1;
      while (filetable.count(
            new QueryWrapper<FileInfo>().eq("parent_id", parentId).eq("user_id", userId)
                  .eq("file_name", newName).eq("status", 0).eq("file_type", type)) > 0) {
         count++;
         newName = fileName + "(" + count + ")";
      }
      return newName;
   }
  //回收/删除用----
  public void dfsFolder(String parentId,String userId,Byte status,List<FileInfo> lists){
      List<FileInfo> children=filetable.list(new QueryWrapper<FileInfo>()
             .eq("parent_id",parentId)
             .eq("user_id",userId).eq("status",status));
      for(FileInfo child:children){
          lists.add(child);
          if(child.getFileType()==0)  dfsFolder(child.getFileId(), userId, status, lists);
      }
  }

   public String saveFileRecord(String userId, String parentId, String fileName, Byte type, String filePath,
         String md5, long fileSize) {
      FileInfo fileInfo = new FileInfo();
      fileInfo.setFileType(type);
      if (type == 0) {
         fileName = toTrueName(parentId, userId, fileName, type);
         fileInfo.setFileName(fileName);
      } else {
         String frontName = fileName.substring(0, fileName.lastIndexOf("."));
         String ext = fileName.substring(fileName.lastIndexOf("."));
         // 用全名查重（之前用去后缀名查，DB 存的是全名，永远匹配不到）
         String tryName = fileName;
         int count = 1;
         while (filetable.count(
               new QueryWrapper<FileInfo>().eq("parent_id", parentId).eq("user_id", userId)
                     .eq("file_name", tryName).eq("status", 0).eq("file_type", type)) > 0) {
            count++;
            tryName = frontName + "(" + count + ")" + ext;
         }
         fileName = tryName;
         fileInfo.setFileName(fileName);
         fileInfo.setFileCategory((byte) FileCategoryEnum.fromExtension(ext).getCode());
         fileInfo.setMd5(md5);
         fileInfo.setFileSize(fileSize);
         fileInfo.setFilePath(filePath);
      }
      fileInfo.setFileId(String.valueOf(IdWorker.getId()));
      fileInfo.setUserId(userId);
      fileInfo.setParentId(parentId);
      fileInfo.setStatus((byte) 0);
      fileInfo.setCreatedTime(LocalDateTime.now());
      fileInfo.setUpdateTime(LocalDateTime.now());
      filetable.save(fileInfo);

      return fileInfo.getFileId();
   }

   // 没人用实际----
   public void fastSave(String md5, String fileName, String parentId) {
      FileInfo exist = filetable.getOne(new QueryWrapper<FileInfo>().eq("md5", md5));
      String userId = getIdFromToken();
      if (exist != null) {
         String filePath = exist.getFilePath();
         long fileSize = exist.getFileSize();
         saveFileRecord(userId, parentId, fileName, (byte) 1, filePath, md5, fileSize);
      } else {
         return;
      }
   }
   
   //上传文件
   public void saveFile(String userId, String parentId, MultipartFile file) throws IOException {
      String fileName = file.getOriginalFilename();
      String fileId = String.valueOf(IdWorker.getId());
      String ext = "";
      if (fileName != null && fileName.lastIndexOf(".") >= 0) {
         ext = fileName.substring(fileName.lastIndexOf("."));
      }
      long fileSzie = file.getSize();

      Path userDir = Paths.get(fileConfig.getTotalPath(), userId);
      Files.createDirectories(userDir);
      Path targetPath = userDir.resolve(fileId + ext);

      file.transferTo(targetPath);
      String md5 = DigestUtils.md5Hex(Files.newInputStream(targetPath));

      FileInfo exist = filetable.getOne(new QueryWrapper<FileInfo>().eq("md5", md5));
      if (exist != null) {
         Files.delete(targetPath);
         String filePath = exist.getFilePath();
         long fileSize = exist.getFileSize();
         saveFileRecord(userId, parentId, fileName, (byte) 1, filePath, md5, fileSize);
      } else {
         String filePath = targetPath.toString();
         saveFileRecord(userId, parentId, fileName, (byte) 1, filePath, md5, fileSzie);
      }
   }

   private void doZip(String userId, String parentId, String parentPath, ZipOutputStream zos) throws IOException {
      // 获取子文件集
      List<FileInfo> items = filetable.list(new QueryWrapper<FileInfo>().eq("user_id", userId)
            .eq("parent_id", parentId).eq("status", (byte) 0));
      for (FileInfo item : items) {
         if (item.getFileType() == 0) {
            doZip(userId, item.getFileId(), parentPath + item.getFileName() + "/", zos);
         } else {
            Path filePath = Paths.get(item.getFilePath());
            if (Files.exists(filePath)) {
               ZipEntry entry = new ZipEntry(parentPath + item.getFileName());
               entry.setSize(item.getFileSize());
               zos.putNextEntry(entry);
               Files.copy(filePath, zos);
               zos.closeEntry();
            }
         }
      }
   }

   // 再url上传参数
   @GetMapping("/list")
   public Result<List<FileInfo>> getList(@RequestParam(name="parentId", defaultValue = "0") String parentId,
         @RequestParam(name="status", defaultValue = "0") String status) {
      String userId = getIdFromToken();
      QueryWrapper<FileInfo> q = new QueryWrapper<FileInfo>().eq("user_id", userId).eq("parent_id", parentId)
            .eq("status", Integer.parseInt(status));
      List<FileInfo> fileInfos = filetable.list(q);
      return Result.success("文件信息传输成功", fileInfos);
   }

   @PostMapping("/upload")
   public Result<?> postMethodName(@RequestParam("file") MultipartFile file,
         @RequestParam(name="parentId", defaultValue = "0") String parentId) {
      if (file.isEmpty()) {
         return Result.fail("未识别到文件");
      }
      String userId = getIdFromToken();
      try {
         saveFile(userId, parentId, file);
         return Result.success("上传成功", null);
      } catch (Exception e) {
         return Result.fail(500, "上传失败");
      }
   }

   @PostMapping("/uploadFolder")
   public Result<?> uploadFolder(@RequestParam("files") MultipartFile[] files,
         @RequestParam("relativePaths") String relativePathsJson,
         @RequestParam(name="parentId", defaultValue = "0") String parentId) {
      // 实际只需要统计文件信息，并以此推出需要的文件夹
      String userId = getIdFromToken();
      try {
         // 根据""分隔分组
         List<String> paths = JSON.parseArray(relativePathsJson, String.class);
         // 路径--->文件夹ID
         Map<String, String> folderCache = new HashMap<>();
         // 对于每一个文件的相对路径
         for (int i = 0; i < files.length; i++) {
            String fullPath = paths.get(i);
            String[] parts = fullPath.split("/");

            String currentParentId = parentId;
            // 当前累计的文件夹路径
            List<String> curPath = new ArrayList<String>();
            // 对于一个相对路径的拆分(不包括最后的文件本身)
            for (int j = 0; j < parts.length - 1; j++) {
               curPath.add(parts[j]);
               String folderKey = String.join("/", curPath);
               if (!folderCache.containsKey(folderKey)) {
                  String newId = saveFileRecord(userId, currentParentId, parts[j], (byte) 0, null, null, 0);
                  folderCache.put(folderKey, newId);
               }
               currentParentId = folderCache.get(folderKey);
            }
            saveFile(userId, currentParentId, files[i]);
         }
         return Result.success("上传成功", null);
      } catch (Exception e) {
         return Result.fail(500, "上传失败");
      }
   }

   @Autowired
   private com.yunpan.backend.util.TokenProvider tokenProvider;

   @GetMapping("/download")
   public void download(@RequestParam("fileId") String fileId,
         @RequestParam(name="token", required=false) String token,
         HttpServletResponse res) throws IOException {
      FileInfo fileInfo = filetable.getById(fileId);
      // 优先从 SecurityContext 取，走 <a> 链接时从 URL 参数兜底
      String userId = getIdFromToken();
      if ("anonymousUser".equals(userId) && token != null && tokenProvider.vaildateToken(token)) {
         userId = tokenProvider.getUserIdFromToken(token);
      }
      if (fileInfo == null || !fileInfo.getUserId().equals(userId)) {
         res.setStatus(403);
         res.setContentType("application/json;charset=UTF-8");
         res.getWriter().write("{\"code\":403,\"message\":\"无权访问\"}");
         return;
      }
      Byte fileType = fileInfo.getFileType();
      // 0文件夹 1文件
      if (fileType == 0) {
         String zipName = URLEncoder.encode(fileInfo.getFileName() + ".zip", StandardCharsets.UTF_8);
         res.setContentType("application/zip");
         res.setHeader("Content-Disposition", "attachment; fileName=\"" + zipName + "\"");
         ZipOutputStream zos = new ZipOutputStream(res.getOutputStream());
         doZip(userId, fileId, fileInfo.getFileName() + "/", zos);
         zos.finish();
         zos.flush();
      } else {
         String filePath = fileInfo.getFilePath();
         String fileName = fileInfo.getFileName();
         Path filePath_p = Paths.get(filePath);
         if (!Files.exists(filePath_p)) {
            res.setStatus(404);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":404,\"message\":\"资源不存在\"}");
            return;
         }
         res.setContentType("application/octet-stream");
         res.setHeader("Content-Disposition",
               "attachment; fileName=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
         res.setContentLengthLong(fileInfo.getFileSize());

         Files.copy(filePath_p, res.getOutputStream());
      }
   }

   @PutMapping("/Recyle")
   public Result<?> toRecyle(@RequestParam("fileId") String fileId) {
       FileInfo fileInfo=filetable.getById(fileId);
       String userId=getIdFromToken();
       Byte fileType=fileInfo.getFileType();
       try{
         if(fileType==(byte)1){
           fileInfo.setStatus((byte)1);
           fileInfo.setRecycleTime(LocalDateTime.now());
           filetable.updateById(fileInfo);
           return Result.success("回收成功", null);
         }else{
          List<FileInfo> files=new ArrayList<FileInfo>();
          dfsFolder(fileId, userId, (byte)0, files);
          for(FileInfo file:files){
             file.setStatus((byte)1);
             file.setRecycleTime(LocalDateTime.now());
             filetable.updateById(file);
          }
          fileInfo.setStatus((byte)1);
          fileInfo.setRecycleTime(LocalDateTime.now());
          filetable.updateById(fileInfo);
          return Result.success("回收成功", null);
         }
       }catch(Exception e){
           return Result.fail("回收失败"+e.getMessage());
       }
   }
   
   @PutMapping("/Recover")
   public Result<?> toReCover(@RequestParam("fileId")String fileId) {
        FileInfo fileInfo=filetable.getById(fileId);
       String userId=getIdFromToken();
       Byte fileType=fileInfo.getFileType();
       try{
           if(fileType==(byte)1){
           fileInfo.setStatus((byte)0);
           fileInfo.setRecycleTime(null);
           filetable.updateById(fileInfo);
          return Result.success("恢复成功", null);
         }else{
          List<FileInfo> files=new ArrayList<FileInfo>();
          dfsFolder(fileId, userId, (byte)1, files);
          for(FileInfo file:files){
             file.setStatus((byte)0);
             file.setRecycleTime(null);
             filetable.updateById(file);
          }
          fileInfo.setStatus((byte)0);
          fileInfo.setRecycleTime(null);
          filetable.updateById(fileInfo);
          return Result.success("恢复成功", null);
         }
       }catch(Exception e){
           return Result.fail("恢复失败"+e.getMessage());
       }
   }

   @PutMapping("/Delete")
   public Result<?> delete(@RequestParam("fileId")String fileId) {
       FileInfo fileInfo=filetable.getById(fileId);
       String userId=getIdFromToken();
       Byte fileType=fileInfo.getFileType();
       try{
           if(fileType==(byte)1){
           fileInfo.setStatus((byte)2);
           // 只有其他活记录都不引用这个文件时才删物理文件
           if (filetable.count(new QueryWrapper<FileInfo>()
               .eq("file_path", fileInfo.getFilePath()).eq("status", 0)) == 0) {
               Files.delete(Paths.get(fileInfo.getFilePath()));
           }
           filetable.updateById(fileInfo);
          return Result.success("彻底删除成功", null);
         }else{
          List<FileInfo> files=new ArrayList<FileInfo>();
          dfsFolder(fileId, userId, (byte)1, files);
          for(FileInfo file:files){
             file.setStatus((byte)2);
             if(file.getFileType()==(byte)1){
                 if (filetable.count(new QueryWrapper<FileInfo>().eq("file_path", file.getFilePath()).eq("status", 0)) == 0) {
                 Files.delete(Paths.get(file.getFilePath()));
                }
             }
             filetable.updateById(file);
          }
          fileInfo.setStatus((byte)2);
          filetable.updateById(fileInfo);
          return Result.success("彻底删除成功", null);
         }
       }catch(Exception e){
           return Result.fail("彻底删除失败"+e.getMessage());
       }
   }
   //创建新文件夹
   @PutMapping("/NewFolder")
   public Result<?> CreateFolder(@RequestParam("parentId")String parentId,@RequestParam("fileName")String fileName) {
        String userId=getIdFromToken();
        try {
           saveFileRecord(userId, parentId, fileName, (byte)0, null, null, 0);
           return Result.success("文件夹创建成功", null);
        } catch (Exception e) {
           return Result.fail("文件夹创建失败"+e.getMessage());
        }
   }

   //剪切移动
   @PutMapping("/Cutto")
   public Result<?>  Cutto(@RequestParam("fileId")String fileId,@RequestParam("desParentId")String desId) {
       String userId=getIdFromToken();
       try{
          FileInfo srcinfo=filetable.getById(fileId);
          srcinfo.setParentId(desId);
          filetable.updateById(srcinfo);
          return Result.success("移动成功", null);
       }catch(Exception e){
          return Result.fail("移动失败"+e.getMessage());
       }
   }
   
  //复制到
   @PutMapping("/Copyto")
   public Result<?> Copyto(@RequestParam("fileId")String fileId,@RequestParam("desParentId")String desId) {
        String userId=getIdFromToken();
        FileInfo srcinfo=filetable.getById(fileId);
        String fileName=srcinfo.getFileName();
        Byte fileType=srcinfo.getFileType();
        try{
            if(fileType==0){
            saveFileRecord(userId, desId, fileName,fileType, null, null, 0);
            return Result.success("复制移动成功", null);
        }else{
            String filePath=srcinfo.getFilePath();
            String md5=srcinfo.getMd5();
            long fileSize=srcinfo.getFileSize();
            saveFileRecord(userId, desId, fileName, fileType, filePath, md5, fileSize);
            return Result.success("复制移动成功", null);
        }
        }catch(Exception e){
             return Result.fail("复制移动失败"+e.getMessage());
        }
   }
   //重命名
   @PutMapping("/Rename")
   public Result<?> putMethodName(@RequestParam("newName")String newName,@RequestParam("fileId")String fileId) {
        try {
          String userId=getIdFromToken();
          FileInfo fileInfo=filetable.getById(fileId);
          Byte fileType=fileInfo.getFileType();
          String parentId=fileInfo.getParentId();
          String fileName=toTrueName(parentId,userId,newName, fileType);
          fileInfo.setFileName(fileName);
          fileInfo.setUpdateTime(LocalDateTime.now());
          filetable.updateById(fileInfo);
          return Result.success("重命名成功", null);
        } catch (Exception e) {
           return Result.fail("重命名失败");
        }
   }
   //略缩图信息--对于图片直接传文件
   @GetMapping({"/PreImage", "/thumbnail"})
   public void  getPreImage(@RequestParam("fileId")String fileId,HttpServletResponse res) throws IOException {
       FileInfo file=filetable.getById(fileId);
       if (file == null || file.getFileType() != 1) {
        res.setStatus(404);
        return;
       }
       Path p = Paths.get(file.getFilePath());
       if (!Files.exists(p)) {
          res.setStatus(404);
           return;
       }
        // 图片类才设 image/*，其他类型前端不调用这里
       String ext = file.getFileName().substring(file.getFileName().lastIndexOf("."));
       res.setContentType("image/" + ext.substring(1));
       // 图片不变，浏览器缓存 7 天，二次加载直接秒出
       res.setHeader("Cache-Control", "public, max-age=86400, immutable");
       Files.copy(p, res.getOutputStream());
   }
   

   //根据文件Id获取文件名
   @PostMapping("/getFileName")
   public Result<?> getFileName(@RequestParam("fileId") String fileId) {
       try{
         FileInfo fileInfo=filetable.getById(fileId);
         String fileName=fileInfo.getFileName();
         return Result.success("文件名获取成功", fileName);
       }catch(Exception e){
          return Result.fail("文件名获取失败");
       }
       
   }
   
}
