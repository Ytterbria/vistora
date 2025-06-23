package com.ytterbria.vistorabackend.model.dto.space;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SpaceEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 序列化ID
     */
    @Serial
    private static final long serialVersionUID = 7397508694810064570L;
}
