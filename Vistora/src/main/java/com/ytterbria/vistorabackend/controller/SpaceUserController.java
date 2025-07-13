package com.ytterbria.vistorabackend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import com.ytterbria.vistorabackend.constant.SpaceUserPermissionConstant;
import com.ytterbria.vistorabackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.ytterbria.vistorabackend.model.entity.SpaceUser;
import com.ytterbria.vistorabackend.model.vo.SpaceUserVO;
import com.ytterbria.vistorabackend.service.SpaceUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/space-user")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 添加成员到空间
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value= SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {

        long id = spaceUserService.addSpaceUser(spaceUserAddRequest);

        return ResultUtils.success(id);
    }

    /**
     * 从空间移除成员
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value= SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {

        return ResultUtils.success(
                spaceUserService.deleteSpaceUser(deleteRequest)
        );
    }

    /**
     * 查询某个成员在某个空间的信息
     */
    @PostMapping("/get")
    @SaSpaceCheckPermission(value= SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询成员信息列表
     */
    @PostMapping("/list")
    @SaSpaceCheckPermission(value= SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        return ResultUtils.success(spaceUserService.listSpaceUser(spaceUserQueryRequest));
    }

    /**
     * 编辑成员信息（设置权限）
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value= SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        return ResultUtils.success(
                spaceUserService.editSpaceUser(spaceUserEditRequest)
        );
    }

    /**
     * 查询我加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
       return ResultUtils.success(
                spaceUserService.listMyTeamSpace(request)
        );
    }
}

