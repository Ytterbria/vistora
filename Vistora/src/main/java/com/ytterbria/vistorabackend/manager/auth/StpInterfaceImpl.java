package com.ytterbria.vistorabackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.constant.SpaceUserPermissionConstant;
import com.ytterbria.vistorabackend.constant.UserConstant;
import com.ytterbria.vistorabackend.enums.SpaceRoleEnum;
import com.ytterbria.vistorabackend.enums.SpaceTypeEnum;
import com.ytterbria.vistorabackend.manager.auth.model.SpaceUserAuthContext;
import com.ytterbria.vistorabackend.model.entity.Picture;
import com.ytterbria.vistorabackend.model.entity.Space;
import com.ytterbria.vistorabackend.model.entity.SpaceUser;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.service.PictureService;
import com.ytterbria.vistorabackend.service.SpaceService;
import com.ytterbria.vistorabackend.service.SpaceUserService;
import com.ytterbria.vistorabackend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private PictureService pictureService;

    @Resource
    @Lazy
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    /**
     * 获取请求获得的 SpaceUserAuthContext 对象
     * @return SpaceUserAuthContext
     */
    private SpaceUserAuthContext getAuthContextByRequest(){
        // 获取当前请求的 HttpServletRequest 对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;

        // 兼容 get 和 post 操作
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        /*
            * 处理请求中的 id 字段，根据不同的模块设置对应的属性
            * 不同的模块可能有不同的 id 字段，
            * 例如 picture、spaceUser、space 等，它们的id字段可能代表不同的含义
            * 可以根据请求的 URI 来判断当前请求属于哪个模块，
         */
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            String requestUri = request.getRequestURI();
            String partUri = requestUri.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(partUri, "/", false);
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
            }
        }
        return authRequest;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     * @param loginId 登录账号id
     * @param    loginType 管理类型(例如区分原本登录逻辑和目前space相关登录逻辑);
     */
    @Override
    public List<String> getPermissionList(Object loginId,String loginType) {
        if (!StpKit.SPACE.loginType.equals(loginType)) {
            return new ArrayList<>();
        }
        //管理员权限列表
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        //如果全部为空,则说明是公共图库,直接给放行
        if (isAllFieldsNull(authContext)) {
            return ADMIN_PERMISSIONS;
        }
        //获取userId
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.NO_AUTH_ERROR, "用户未登录");
        Long userId = loginUser.getId();
        //尝试从上下文中获得SpaceUser对象
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (ObjUtil.isNotNull(spaceUser)) {
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        //如果有spaceUserId,则必然是团队空间,所以查库获得SpaceUser对象
        Long spaceUserId = authContext.getSpaceUserId();
        if (ObjUtil.isNotNull(spaceUserId)) {
            spaceUser = spaceUserService.getById(spaceUserId);
            ThrowUtils.throwIf(ObjUtil.isNull(spaceUser), ErrorCode.NOT_FOUND_ERROR, "空间用户不存在");
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId,spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId,userId)
                    .one();
            //说明不是该团队空间的用户
            if (loginSpaceUser == null){
                return new ArrayList<>();
            }
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());

        }
        //如果没有spaceUserId,再尝试通过spaceId或userId查到Space对象
        Long spaceId = authContext.getSpaceId();
        if (ObjUtil.isNull(spaceId)){
            //如果没有spaceId,再尝试pictureId获得picture和space对象
            Long pictureId = authContext.getPictureId();
            //如果图片Id也没有,则直接放行
            if(ObjUtil.isNull(pictureId)){
                return ADMIN_PERMISSIONS;
            }
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId,pictureId)
                    .select(Picture::getId,Picture::getSpaceId,Picture::getUserId)
                    .one();
            ThrowUtils.throwIf(ObjUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            spaceId = picture.getSpaceId();
            //图片没有spaceId则说明是公共图库,仅管理员和本人可操作
            if (spaceId == null){
                if(picture.getUserId().equals(userId) || userService.isAdmin(loginUser)){
                    return ADMIN_PERMISSIONS;
                } else {
                    //如果不是管理员或本人,则只能查看图片
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        // 获取 Space 对象
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }
        // 根据 Space 类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE_SPACE.getValue()) {
            // 私有空间，仅本人或管理员有权限
            if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            } else {
                return new ArrayList<>();
            }
        } else if (space.getSpaceType().equals(SpaceTypeEnum.TEAM_SPACE.getValue())){
            // 团队空间，查询 SpaceUser 并获取角色和权限
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }

        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 判断对象的所有字段是否都为空
     * @return boolean
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }

}
