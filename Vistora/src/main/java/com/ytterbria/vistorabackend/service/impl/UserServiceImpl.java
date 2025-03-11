package com.ytterbria.vistorabackend.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.constant.UserConstant;
import com.ytterbria.vistorabackend.enums.UserRoleEnum;
import com.ytterbria.vistorabackend.model.dto.user.*;
import com.ytterbria.vistorabackend.model.vo.LoginUserVO;
import com.ytterbria.vistorabackend.model.vo.UserManageVO;
import generator.domain.User;
import com.ytterbria.vistorabackend.service.UserService;
import com.ytterbria.vistorabackend.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public LoginUserVO userLogin(UserLoginRequest request, HttpServletRequest httpServletRequest) {
         String userAccount = request.getUserAccount();
         String password = request.getUserPassword();
         String encryptPassword = getEncryptPassword(password);
         //校验参数
        if(StrUtil.hasBlank(userAccount,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //校验用户账号是否存在
        queryWrapper.eq("userAccount",userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不存在");
        }
        //校验密码是否正确
        if(!user.getUserPassword().equals(encryptPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
        //登录成功,记录登录状态,将它保存在session中
        httpServletRequest.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS,user);

        //返回登录用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();

        /*
         * 将User对象中的属性复制到LoginUserVO对象中
         * 不存在的属性值将会被置为null,
         */
        BeanUtils.copyProperties(user,loginUserVO);

        return loginUserVO;

    }

    @Override
    public UserManageVO getUserManageVO(User user) {
        if (user == null){
            return null;
        }
        UserManageVO userManageVO = new UserManageVO();
        BeanUtils.copyProperties(user,userManageVO);
        return userManageVO;




    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest request) {
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Long id = request.getId();
        String userAccount = request.getUserAccount();
        String userRole = request.getUserRole();
        String userName = request.getUserName();
        String userProfile = request.getUserProfile();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        return queryWrapper
                .eq(ObjUtil.isNotNull(id),"id",id)
                .eq(StrUtil.isNotBlank(userRole),"userRole",userRole)
                .like(StrUtil.isNotBlank(userAccount),"userAccount",userAccount)
                .like(StrUtil.isNotBlank(userName),"userName",userName)
                .like(StrUtil.isNotBlank(userProfile),"userProfile",userProfile)
                .orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
    }

    @Override
    public User getLoginUserInfo(HttpServletRequest httpServletRequest) {
        Object userOBJ = httpServletRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);

        User currentUser = (User) userOBJ;
        if (currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        currentUser = this.getById(currentUser.getId());
        if (currentUser == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户信息获取失败");
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest httpServletRequest){
        Object userOBJ = httpServletRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (userOBJ == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        httpServletRequest.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
        return true;
    }

    @Override
    public Long addUser(UserAddRequest request) {
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);

        User user = new User();
        BeanUtils.copyProperties(request,user);
        final String defaultPassword = UserConstant.DEFAULT_PASSWORD;
        String encryptedPassword = this.getEncryptPassword(defaultPassword);
        user.setUserPassword(encryptedPassword);
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR);
        return user.getId();
    }

    @Override
    public UserManageVO getUserManageVOById(Long userId) {
        ThrowUtils.throwIf(userId <= 0 , ErrorCode.PARAMS_ERROR);
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        return this.getUserManageVO(user);

    }

    @Override
    public User getUserById(Long userId) {
        ThrowUtils.throwIf(userId <= 0 , ErrorCode.PARAMS_ERROR);
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        return user;
    }

    @Override
    public boolean deleteUser(DeleteRequest request) {
        if (request == null || request.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(request.getId());
    }

    @Override
    public List<UserManageVO> getUserManageVOList(List<User> userList) {
        if (CollectionUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        List<UserManageVO> userManageVOList = new ArrayList<>();
        return userList
                .stream()
                .map(this :: getUserManageVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserManageVO> listUserVO(UserQueryRequest request) {
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        Page<User> userPage = this.page(
                new Page<>(current,pageSize),
                this.getQueryWrapper(request)
                );
        Page<UserManageVO> userManageVOPage = new Page<>(current,pageSize,userPage.getTotal());
        List<UserManageVO> userManageVOList = this.getUserManageVOList(userPage.getRecords());
        userManageVOPage.setRecords(userManageVOList);
        return userManageVOPage;
    }

    @Override
    public boolean updateUser(UserUpdateRequest request) {
        if (request == null || request.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(request,user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR);
        return true;
    }
}




