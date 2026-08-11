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
@TableName("user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一uid
     */
    @TableId("user_id")
    private String userId;

    @TableField("password")
    private String password;

    @TableField("nick_name")
    private String nickname;

    @TableField("email")
    private String email;

    /**
     * 头像url
     */
    @TableField("avatar")
    private String avatar;

    @TableField("total_space")
    private Long totalSpace;

    @TableField("used_space")
    private Long usedSpace;

    /**
     * 注册时间
     */
    @TableField("register_date")
    private LocalDateTime registerDate;

    /**
     * 最后登录时间
     */
    @TableField("last_date")
    private LocalDateTime lastDate;

    /**
     * 0=封禁 1=启用
     */
    @TableField("status")
    private Byte status;
}
