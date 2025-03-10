package com.ytterbria.vistorabackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -6914606666393299071L;

    /**
     * 用户账号
      */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

}
