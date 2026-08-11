package com.yunpan.backend.service.impl;

import com.yunpan.backend.entity.UserInfo;
import com.yunpan.backend.mapper.UserInfoMapper;
import com.yunpan.backend.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hurricanecarry
 * @since 2026-06-13
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
