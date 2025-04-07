package com.ytterbria.vistorabackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 3707609051600367812L;

    /**
     * 图片id
     */
    private Long id;


}
