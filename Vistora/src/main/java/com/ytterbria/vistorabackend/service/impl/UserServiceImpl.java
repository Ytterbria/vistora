package com.ytterbria.vistorabackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.enums.UserRoleEnum;
import com.ytterbria.vistorabackend.model.dto.UserRegisterRequest;
import generator.domain.User;
import com.ytterbria.vistorabackend.service.UserService;
import com.ytterbria.vistorabackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author lenovo
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-28 22:56:32
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Override
    public long userRegister(UserRegisterRequest request) {
        String userAccount = request.getUserAccount();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        // 验空
        if (StrUtil.hasBlank(userAccount,password,confirmPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        // 验证合理性
        if (password.length() < 8 || password.length() > 511){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不合法");
        }
        if (!password.equals(confirmPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }

        // 查询库中用户是否已经存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }

        // 用户密码加密,存储到数据库
        String encryptPassword = getEncryptPassword(password);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean result = this.save(user);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败,数据库错误");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String password){
        // 加密密码
        final String SALT = "YtterbriaVistorabackend";
        return DigestUtils.md5DigestAsHex((SALT +password).getBytes());
    }
}




