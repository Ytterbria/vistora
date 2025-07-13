package com.ytterbria.vistorabackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.enums.SpaceRoleEnum;
import com.ytterbria.vistorabackend.mapper.SpaceUserMapper;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.ytterbria.vistorabackend.model.entity.Space;
import com.ytterbria.vistorabackend.model.entity.SpaceUser;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.SpaceUserVO;
import com.ytterbria.vistorabackend.service.SpaceService;
import com.ytterbria.vistorabackend.service.SpaceUserService;
import com.ytterbria.vistorabackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper,SpaceUser>
implements SpaceUserService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceService spaceService;

    @Override
    public Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest){
        ThrowUtils.throwIf(ObjUtil.isNull(spaceUserAddRequest),ErrorCode.PARAMS_ERROR,"空间用户添加请求不能为空");
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest,spaceUser);
        validSpaceUser(spaceUser,true);

        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "添加空间用户失败");
        return spaceUser.getId();

    }

    @Override
    public void validSpaceUser(SpaceUser spaceUser,boolean intention){
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR, "空间用户不能为空");
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (intention){
            ThrowUtils.throwIf(ObjUtil.hasEmpty(spaceId,userId),ErrorCode.PARAMS_ERROR,"空间ID和用户ID不能为空");
            User user = userService.getById(userId);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.hasNull(user,space),ErrorCode.NOT_FOUND_ERROR,"用户或空间不存在");
        }
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(ObjUtil.hasNull(spaceRole,spaceRoleEnum),ErrorCode.PARAMS_ERROR,"空间角色不存在");
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest){
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId),"spaceId",spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole),"spaceRole",spaceRole);
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        return queryWrapper;
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser){
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        //封装用户信息
        Long userId = spaceUser.getUserId();
        if (ObjUtil.isNotNull(userId) && userId > 0){
            User user = userService.getById(userId);
            if (ObjUtil.isNotNull(user)){
                spaceUserVO.setUser(userService.getLoginUserVO(user));
            }
        }
        //封装空间信息
        Long spaceId = spaceUser.getSpaceId();
        if(ObjUtil.isNotNull(spaceId) && spaceId > 0){
            Space space = spaceService.getById(spaceId);
            if (ObjUtil.isNotNull(space)){
                spaceUserVO.setSpace(spaceService.getSpaceVO(space));
            }
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList){
        if (CollUtil.isEmpty(spaceUserList)){
            return Collections.emptyList();
        }

        // 使用流式处理将 SpaceUser 列表转换为 SpaceUserVO 列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream()
                .map(SpaceUserVO::objToVo)
                .toList();

        //1.收集需要关联查询的用户的ID和空间ID
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());

        //2.批量查询用户和空间
        Map<Long,User> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId,user->user));

        Map<Long,Space> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.toMap(Space::getId,space->space));
        //3.填充空间用户视图对象列表
        spaceUserVOList.forEach(spaceUserVO ->{
            Long userId = spaceUserVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId);
            }
            spaceUserVO.setUser(userService.getLoginUserVO(user));
            Long spaceId = spaceUserVO.getSpaceId();
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)){
                space = spaceIdSpaceListMap.get(spaceId);
            }
            spaceUserVO.setSpace(spaceService.getSpaceVO(space));
        });
        return spaceUserVOList;
    }

    @Override
    public Boolean deleteSpaceUser(DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(ObjUtil.isNull(deleteRequest), ErrorCode.PARAMS_ERROR, "删除请求不能为空");
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(ObjUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR, "ID不能为空或无效");

        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除空间用户失败");
        return true;
    }

    @Override
    public SpaceUser getSpaceUser(SpaceUserQueryRequest spaceUserQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = this.getOne(this.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return spaceUser;
    }

    @Override
    public List<SpaceUserVO> listSpaceUser(SpaceUserQueryRequest spaceUserQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        List<SpaceUser> spaceUserList = this.list(this.getQueryWrapper(spaceUserQueryRequest));
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 转换为视图对象列表
        return this.getSpaceUserVOList(spaceUserList);
    }

    @Override
    public Boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest) {
        if (spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        // 数据校验
        this.validSpaceUser(spaceUser, false);
        // 判断是否存在
        long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = this.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = this.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public List<SpaceUserVO> listMyTeamSpace(HttpServletRequest request){
        User loginUser = userService.getLoginUserInfo(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = this.list(
                this.getQueryWrapper(spaceUserQueryRequest)
        );
        return this.getSpaceUserVOList(spaceUserList);
    }
}
