package com.ytterbria.vistorabackend.aliyun.dto.pic;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class CreateImageTaskResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -5062615753435466507L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private String status;
}
