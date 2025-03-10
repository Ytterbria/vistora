package com.ytterbria.vistorabackend.controller;

import com.ytterbria.vistorabackend.annotation.AuthCheck;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import com.ytterbria.vistorabackend.constant.UserConstant;
import com.ytterbria.vistorabackend.model.dto.user.UserLoginRequest;
import com.ytterbria.vistorabackend.model.dto.user.UserRegisterRequest;
import com.ytterbria.vistorabackend.model.vo.LoginUserVO;
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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
        userService.userLogout(httpServletRequest);
        return ResultUtils.success(true);
    }

}
