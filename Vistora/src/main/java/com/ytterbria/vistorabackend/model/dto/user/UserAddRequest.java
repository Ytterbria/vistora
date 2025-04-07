package com.ytterbria.vistorabackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

//用户添加请求参数
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = -6737548619053266893L;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色(user,admin)
     */
    private String userRole;

}
