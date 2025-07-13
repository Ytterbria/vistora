package com.ytterbria.vistorabackend.manager.aliyun.dto.pic;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CreateImageTaskRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6719041995675243206L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户输入的提示词
     */
    private String prompt;

}
