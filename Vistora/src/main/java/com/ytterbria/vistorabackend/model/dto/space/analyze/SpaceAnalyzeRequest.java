package com.ytterbria.vistorabackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SpaceAnalyzeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 2615344419483041405L;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 查询公共图库
     */
    private Boolean queryPub;

    /**
     * 查询全空间
     */
    private Boolean queryAll;
}
