package com.ytterbria.vistorabackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.enums.SpaceLevelEnum;
import com.ytterbria.vistorabackend.mapper.SpaceMapper;
import com.ytterbria.vistorabackend.model.dto.space.SpaceAddRequest;
import com.ytterbria.vistorabackend.model.dto.space.SpaceQueryRequest;
import com.ytterbria.vistorabackend.model.dto.space.SpaceUpdateRequest;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.LoginUserVO;
import com.ytterbria.vistorabackend.model.vo.SpaceVO;
import com.ytterbria.vistorabackend.model.vo.UserManageVO;
import com.ytterbria.vistorabackend.service.SpaceService;
import com.ytterbria.vistorabackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.ytterbria.vistorabackend.model.entity.Space;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void validSpace(Space space,boolean intention){
        ThrowUtils.throwIf(ObjUtil.isEmpty(space), ErrorCode.PARAMS_ERROR);
        //从对象中获取属性值进行校验
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        if (intention){//如果是创建意愿
            ThrowUtils.throwIf(StrUtil.isEmpty(spaceName), ErrorCode.PARAMS_ERROR,"空间名称不能为空");
            ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR,"空间级别不能为空");
        }
        ThrowUtils.throwIf(ObjUtil.isNotEmpty(spaceLevel) && ObjUtil.isEmpty(spaceLevelEnum), ErrorCode.PARAMS_ERROR,"不存在的空间级别");
        ThrowUtils.throwIf(ObjUtil.isNotEmpty(spaceName) && spaceName.length() > 100,ErrorCode.PARAMS_ERROR,"空间名称过长");
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space){
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (ObjUtil.isNotNull(spaceLevelEnum)){
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null){
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null){
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest){
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest,space);
        this.fillSpaceBySpaceLevel(space);
        this.validSpace(space,false);
        long id = spaceUpdateRequest.getId();
        Space oldSpace = this.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(oldSpace),ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        boolean result = this.updateById(space);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"更新空间失败");
        return true;
    }

    @Override
    public SpaceVO getSpaceVO(Space space){
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0){
            User user = userService.getById(userId);
            LoginUserVO userVO = userService.getLoginUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }
    @Override
    public Page<SpaceVO> getSpaceVOPage (Page<Space> spacePage){
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(),spacePage.getSize(),spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)){
            return spaceVOPage;
        }

        //space对象列表 -> spaceVO封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO :: objToVo)
                .collect(Collectors.toList());

        //关联查询用户信息
        Set<Long> userIdSet = spaceList.stream()
                .map(Space :: getUserId)
                .collect(Collectors.toSet());

        Map<Long,User> userIdUserMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.toMap(User::getId,user -> user));

        spaceVOList.forEach(spaceVO ->{
           Long userId = spaceVO.getUserId();
           if (userIdUserMap.containsKey(userId)){
               spaceVO.setUser(userService.getLoginUserVO(userIdUserMap.get(userId)));
           }
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser){
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser),ErrorCode.NOT_LOGIN_ERROR,"用户未登录");

        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest,space);
        if (StrUtil.isEmpty(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if (ObjUtil.isNull(spaceAddRequest.getSpaceLevel())){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }

        this.fillSpaceBySpaceLevel(space);
        this.validSpace(space,true);


        Long userId = loginUser.getId();
        space.setUserId(userId);
        if( SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"无权限创建此级别的空间");
        }
        //加锁确保同一用户仅能有一个空间
        Map<Long,Object> lockMap = new ConcurrentHashMap<>();
        Object lock = lockMap.computeIfAbsent(userId, id -> new Object());
        synchronized(lock){
            Long newSpaceId = transactionTemplate.execute(status ->{
               boolean exists = this.lambdaQuery().eq(Space::getUserId,userId).exists();
               ThrowUtils.throwIf(exists,ErrorCode.OPERATION_ERROR,"每个用户仅能创建一个空间");
               boolean result = this.save(space);
               ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"创建空间失败");
               return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest){
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();

        if (ObjUtil.isNull(spaceQueryRequest)){
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(ObjUtil.isNotEmpty(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}
