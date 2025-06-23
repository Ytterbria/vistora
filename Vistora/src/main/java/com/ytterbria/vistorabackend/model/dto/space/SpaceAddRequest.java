package com.ytterbria.vistorabackend.model.dto.space;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SpaceAddRequest implements Serializable {

    /**
     * 序列化ID
     */
    @Serial
    private static final long serialVersionUID = 1101151587992316026L;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级
     */
    private Integer spaceLevel;
}
