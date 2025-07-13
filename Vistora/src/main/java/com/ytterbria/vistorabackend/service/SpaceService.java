package com.ytterbria.vistorabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ytterbria.vistorabackend.model.dto.space.SpaceAddRequest;
import com.ytterbria.vistorabackend.model.dto.space.SpaceQueryRequest;
import com.ytterbria.vistorabackend.model.dto.space.SpaceUpdateRequest;
import com.ytterbria.vistorabackend.model.entity.Space;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;


public interface SpaceService extends IService<Space> {
     /**
      * 校验空间是否可用或者可以创建,以及创建中的一些参数校验
      * @param space 空间实体
      * @param intention 是否是创建空间的意图
      */
     void validSpace(Space space,boolean intention);

    /**
     * 校验空间权限
     *
     * @param space     空间实体
     * @param loginUser 登录用户
     */
    void checkSpaceAuth(Space space, User loginUser);

    /**
      * 根据空间等级填充空间信息
      * @param space 空间实体
      */
     void fillSpaceBySpaceLevel(Space space);

     /**
      * 根据空间id更新空间信息
      * @param spaceUpdateRequest 空间更新请求实体
      * @return 是否更新成功
      */
     boolean updateSpace(SpaceUpdateRequest spaceUpdateRequest);

     Page<SpaceVO> getSpaceVOPage(Page<Space> page);

     SpaceVO getSpaceVOById(long id, HttpServletRequest request);

     SpaceVO getSpaceVO(Space space);

     QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

     long addSpace (SpaceAddRequest spaceAddRequest, User loginUser);
}
