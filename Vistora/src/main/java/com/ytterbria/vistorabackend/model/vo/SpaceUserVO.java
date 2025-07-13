package com.ytterbria.vistorabackend.model.vo;

import cn.hutool.core.util.ObjUtil;
import com.ytterbria.vistorabackend.model.entity.Space;
import com.ytterbria.vistorabackend.model.entity.SpaceUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class SpaceUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7409645174730053810L;

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户封装
     */
    private LoginUserVO user;

    /**
     * 空间信息封装
     */
    private SpaceVO space;

    public static SpaceUser voTOObj(SpaceUserVO spaceUserVO) {
        if (ObjUtil.isNull(spaceUserVO)){
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserVO,spaceUser);
        return spaceUser;
    }

    public static SpaceUserVO objToVo(SpaceUser spaceUser){
        if (spaceUser == null){
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtils.copyProperties(spaceUser,spaceUserVO);
        return spaceUserVO;
    }

}
