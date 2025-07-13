package com.ytterbria.vistorabackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SpaceUserEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5373314610745026919L;

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

}
