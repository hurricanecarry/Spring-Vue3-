package com.yunpan.backend.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunpan.backend.entity.UserInfo;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hurricanecarry
 * @since 2026-06-13
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}
