package com.ytterbria.vistorabackend.service;

import com.ytterbria.vistorabackend.model.dto.UserRegisterRequest;
import generator.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lenovo
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-28 22:56:32
*/
public interface UserService extends IService<User> {
        long userRegister(UserRegisterRequest request);

        String getEncryptPassword(String password);
}
