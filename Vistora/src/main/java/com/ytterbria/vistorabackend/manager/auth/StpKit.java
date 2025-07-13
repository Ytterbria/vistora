package com.ytterbria.vistorabackend.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * StpKit 提供了对 Satoken 的封装,可以实现多账号权限体系的会话管理
 * 主要用于获取默认的会话对象和 Space 会话对象。
 */
@Component
public class StpKit {
    public static final String SPACE_TYPE = "space";

    /**
     * 默认原生的会话对象
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * Space 会话对象
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
