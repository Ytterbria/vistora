package com.ytterbria.vistorabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.model.dto.user.*;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.LoginUserVO;
import com.ytterbria.vistorabackend.model.vo.UserManageVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface UserService extends IService<User> {
        /**
         * 判断是否为管理员
         *
         * @param user 用户实体
         * @return 是否为管理员
         */
        boolean isAdmin(User user);

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
         * 获得当前登录用户信息
         */
        User getLoginUserInfo(HttpServletRequest httpServletRequest);


        /**
         * 退出登录状态
         */
        boolean userLogout(HttpServletRequest httpServletRequest);

        /**
         * 管理员创建用户
         * @param request 自定义的用户添加请求体
         * @return 添加成功的用户id
         */
        Long addUser(UserAddRequest request);

        /**
         * 管理员根据用户id获得封装类UserManageVO
         * @param userId 用户id
         * @return UserManageVO 封装后用于管理的用户信息
         */
        UserManageVO getUserManageVOById(Long userId);

        /**
         * 管理员根据用户id获得用户实体
         * @param userId 用户id
         * @return 用户实体
         */
        User getUserById(Long userId);

        /**
         * 管理员删除用户
         * @param request 自定义的用户删除请求体
         * @return Boolean 是否删除成功
        boolean deleteUser(DeleteRequest request);



        /**
         * 管理员更新用户信息
         * @param request 自定义的用户更新请求体
         * @return Boolean 是否更新成功
         */
         boolean updateUser(UserUpdateRequest request);

        /**
         * 管理员批量删除用户
         * @param request
         * @return
         */
         boolean deleteUser(DeleteRequest request);

         /**
         * 管理员分页查询用户信息
         * @param request 自定义的用户查询请求体
         * @return Page<User> 分页查询结果
         */
         Page<UserManageVO> listUserVO(UserQueryRequest request);


        /**
         * 获得被脱敏的登录用户信息
         * @param user 用户实体
         * @return 脱敏后的用户信息实体
         */
        LoginUserVO getLoginUserVO(User user);

        /**
         * 供管理员获得用户脱敏信息
         * @param user 用户实体
         * @return 脱敏后的用户管理信息实体
         */
        UserManageVO getUserManageVO(User user);

        /**
         * 将查询请求转化为QueryWrapper对象
         * @param request 查询请求
         * @return QueryWrapper对象
         */
        QueryWrapper<User> getQueryWrapper(UserQueryRequest request);

        List<UserManageVO> getUserManageVOList(List<User> userList);
}
