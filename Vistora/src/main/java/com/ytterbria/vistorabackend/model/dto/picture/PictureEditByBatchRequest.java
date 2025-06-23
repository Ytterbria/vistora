package com.ytterbria.vistorabackend.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditByBatchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -5320394578198857140L;

    /**
     * 图片ID列表
     */
    private List<Long> pictureIdList;

    /**
     * 空间Id
     */
    private Long spaceId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 命名规则
     */
    private String nameRule;


}
