package com.ytterbria.vistorabackend.controller;

import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import com.ytterbria.vistorabackend.model.dto.UserRegisterRequest;
import com.ytterbria.vistorabackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
