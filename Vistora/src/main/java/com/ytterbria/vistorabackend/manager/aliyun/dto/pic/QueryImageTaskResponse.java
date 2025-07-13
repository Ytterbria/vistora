package com.ytterbria.vistorabackend.manager.aliyun.dto.pic;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class QueryImageTaskResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -8971818544591046462L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 返回的创建成功的图片URL
     */
    private List<String> imageUrls = new ArrayList<>();

}
