package com.ytterbria.vistorabackend.manager.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SpaceUserAuthConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = -5826556189737728913L;

    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;
}
