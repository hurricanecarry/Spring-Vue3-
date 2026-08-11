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
@TableName("share_info")
public class ShareInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分享id（分享地址: share/分享id）
     */
    @TableId("share_id")
    private String shareId;

    /**
     * 分享者id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 分享文件id
     */
    @TableField("file_id")
    private String fileId;

    
    @TableField("file_name")
    private String fileName;

    /**
     * 提取码(五位随机)
     */
    @TableField("share_code")
    private String shareCode;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 0=公开  1=需要提取码
     */
    @TableField("share_type")
    private Byte shareType;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 0=有效  1=已取消  2=已过期
     */
    @TableField("status")
    private Byte status;

    @TableField("created_time")
    private LocalDateTime createdTime;
}
