package com.ytterbria.vistorabackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ytterbria.vistorabackend.annotation.AuthCheck;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import com.ytterbria.vistorabackend.constant.UserConstant;
import com.ytterbria.vistorabackend.model.dto.user.*;
import com.ytterbria.vistorabackend.model.vo.LoginUserVO;
import com.ytterbria.vistorabackend.model.vo.UserManageVO;
import com.ytterbria.vistorabackend.service.UserService;
import generator.domain.User;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request){
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(userService.userRegister(request));
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin (@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(userService.userLogin(userLoginRequest, httpServletRequest));
    }

    @GetMapping("/info")
    public BaseResponse<LoginUserVO> getLoginUserInfo(HttpServletRequest httpServletRequest){
        User loginUser = userService.getLoginUserInfo(httpServletRequest);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest httpServletRequest){
        Boolean result = userService.userLogout(httpServletRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest request){
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.addUser(request));
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id){
         ThrowUtils.throwIf(id == 0, ErrorCode.PARAMS_ERROR);
         return ResultUtils.success(userService.getUserById(id));
    }

    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserManageVO> getUserManageVOById(long id){
         ThrowUtils.throwIf(id == 0, ErrorCode.PARAMS_ERROR);
         return ResultUtils.success(userService.getUserManageVOById(id));
        }


    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(DeleteRequest deleteRequest) {
    ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,ErrorCode.PARAMS_ERROR);
    return ResultUtils.success(userService.deleteUser(deleteRequest));
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.updateUser(userUpdateRequest));
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserManageVO>> listUserVO(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.listUserVO(userQueryRequest));
    }

}
