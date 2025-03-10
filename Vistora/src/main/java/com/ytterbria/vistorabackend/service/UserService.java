package com.ytterbria.vistorabackend.service;

import com.ytterbria.vistorabackend.model.dto.user.UserLoginRequest;
import com.ytterbria.vistorabackend.model.dto.user.UserRegisterRequest;
import com.ytterbria.vistorabackend.model.vo.LoginUserVO;
import generator.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;


public interface UserService extends IService<User> {
        /**
         * 用户注册
         * @param  request 自定义的用户注册请求体
         * @return 注册成功的用户id
         */
        long userRegister(UserRegisterRequest request);

        /**
         * 加密密码
         * @param password 原始明文密码
         * @return 加密后的密码
         */
        String getEncryptPassword(String password);

        /**
         * 用户登录
         * @param request 自定义的用户登录请求体
         * @param httpServletRequest
         * 其中提供了绘画管理功能,可以获取或者创建session,
         * 注册不需要,因为没有session,cookie或客户端信息
         * @return LoginUserVO ,脱敏后的用户信息
         */
        LoginUserVO userLogin(UserLoginRequest request, HttpServletRequest httpServletRequest);

        /**
         * 获得被脱敏的用户信息
         * @param user 用户实体
         * @return 脱敏后的用户信息实体
         */
        LoginUserVO getLoginUserVO(User user);

        /**
         * 获得当前登录用户信息
         */
        User getLoginUserInfo(HttpServletRequest httpServletRequest);

        /**
         * 退出登录状态
         */
        boolean userLogout(HttpServletRequest httpServletRequest);
}
