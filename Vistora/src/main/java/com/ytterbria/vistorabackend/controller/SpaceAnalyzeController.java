package com.ytterbria.vistorabackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import com.ytterbria.vistorabackend.model.dto.space.analyze.*;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.service.SpaceAnalyzeService;
import com.ytterbria.vistorabackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    private UserService userService;


    /**
     * 获取空间使用情况分析 可获得空间使用比例,用量等信息
     * @param spaceUsageAnalyzeRequest 请求参数
     * @param request HttpServletRequest
     * @return BaseResponse<SpaceUsageAnalyzeResponse>
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request
            ) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUserInfo(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);
    }

    /**
     * 获取空间分类分析 获得某个分类的图片数量,占用大小
     * @param spaceCategoryAnalyzeRequest 请求参数
     * @param request HttpServletRequest
     * @return BaseResponse<List<SpaceCategoryAnalyzeResponse>>
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            HttpServletRequest request
            ){

        // 获取当前登录用户
        User loginUser = userService.getLoginUserInfo(request);

        return ResultUtils.success(spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest,loginUser));
    }

    /**
     * 获取空间标签分析 获得每个标签对应的数量,用于生成词云图
     * @param spaceTagAnalyzeRequest 请求参数
     * @param request HttpServletRequest
     * @return BaseResponse<List<SpaceTagAnalyzeResponse>>
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUserInfo(request);
        List<SpaceTagAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,HttpServletRequest request){
        // 获取当前登录用户
        User loginUser = userService.getLoginUserInfo(request);
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

}
