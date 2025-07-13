package com.ytterbria.vistorabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.ytterbria.vistorabackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.ytterbria.vistorabackend.model.entity.SpaceUser;
import com.ytterbria.vistorabackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     *
     * @param spaceUserAddRequest 空间用户添加请求
     * @return 新增的空间用户 ID
     */
    Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 验证空间用户
     *
     * @param spaceUser 空间用户实体
     * @param intention 是否有意图（例如：是否需要验证权限）
     */
    void validSpaceUser(SpaceUser spaceUser,boolean intention);

    /**
     * 获取查询条件包装器
     *
     * @param spaceUserQueryRequest 空间用户查询请求
     * @return 查询条件包装器
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户的视图对象
     *
     * @param spaceUser 空间用户实体
     * @return 空间用户视图对象
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser);

    /**
     * 将空间用户列表转换为视图对象列表
     *
     * @param spaceUserList 空间用户实体列表
     * @return 空间用户视图对象列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 删除空间用户
     *
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    Boolean deleteSpaceUser(DeleteRequest deleteRequest);

    /**
     * 获取空间用户
     *
     * @param spaceUserQueryRequest 空间用户查询请求
     * @return 空间用户实体
     */
    SpaceUser getSpaceUser(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 列出空间用户视图对象
     *
     * @param spaceUserQueryRequest 空间用户查询请求
     * @return 空间用户视图对象列表
     */
    List<SpaceUserVO> listSpaceUser(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 列出空间用户视图对象（分页）
     *
     * @param spaceUserEditRequest 空间用户查询请求
     * @return 空间用户视图对象列表
     */
    Boolean editSpaceUser(SpaceUserEditRequest spaceUserEditRequest);

    /**
     * 列出我的团队空间
     *
     * @param request HTTP 请求
     * @return 我的团队空间视图对象列表
     */
    List<SpaceUserVO> listMyTeamSpace(HttpServletRequest request);
}
