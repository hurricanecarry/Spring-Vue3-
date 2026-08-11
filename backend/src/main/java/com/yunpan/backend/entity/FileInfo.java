package com.yunpan.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author hurricanecarry
 * @since 2026-06-13
 */
@Getter
@Setter
@ToString
@TableName("file_info")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("file_id")
    private String fileId;

    @TableField("user_id")
    private String userId;

    /**
     * 父目录id
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 0=文件夹   1=文件
     */
    @TableField("file_type")
    private Byte fileType;

    /**
     * 1=视频 ，2=音频  ，3=文档，4=压缩包，5=其他，6=图片
     */
    @TableField("file_category")
    private Byte fileCategory;

    @TableField("file_path")
    private String filePath;

    /**
     * 字节为单位
     */
    @TableField("file_size")
    private Long fileSize;

    @TableField("md5")
    private String md5;

    /**
     * 0=正常，1=回收站 ，2=彻底删除
     */
    @TableField("status")
    private Byte status;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 进入回收站的时间（30天自动清理）
     */
    @TableField("recycle_time")
    private LocalDateTime recycleTime;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
}
