package com.ytterbria.vistorabackend.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3707609051600367812L;

    /**
     * 图片id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 文件地址
     */
    private String fileUrl;


}
