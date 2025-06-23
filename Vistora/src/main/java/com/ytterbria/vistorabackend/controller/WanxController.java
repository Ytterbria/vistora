package com.ytterbria.vistorabackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.ytterbria.vistorabackend.aliyun.api.WanxManager;
import com.ytterbria.vistorabackend.aliyun.dto.pic.CreateImageTaskRequest;
import com.ytterbria.vistorabackend.aliyun.dto.pic.CreateImageTaskResponse;
import com.ytterbria.vistorabackend.aliyun.dto.pic.QueryImageTaskResponse;
import com.ytterbria.vistorabackend.annotation.AuthCheck;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/wanx")
public class WanxController {
    @Resource
    private WanxManager wanxManager;

    @PostMapping("/create/task")
    @AuthCheck(mustRole="admin") // 仅管理员可以访问
    public BaseResponse<CreateImageTaskResponse> createImageTask(
            @RequestBody CreateImageTaskRequest createImageTaskRequest,
            HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(createImageTaskRequest), ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(wanxManager.createImageTask(createImageTaskRequest));
    }

    @GetMapping("/task/{taskId}")
    @AuthCheck(mustRole="admin") // 仅管理员可以访问
    public BaseResponse<QueryImageTaskResponse> queryImageTask(@PathVariable String taskId,
                                                               HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(taskId), ErrorCode.PARAMS_ERROR);

        QueryImageTaskResponse queryImageTaskResponse = wanxManager.queryTask(taskId);
        ThrowUtils.throwIf(ObjUtil.isEmpty(queryImageTaskResponse), ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(queryImageTaskResponse);
    }
}