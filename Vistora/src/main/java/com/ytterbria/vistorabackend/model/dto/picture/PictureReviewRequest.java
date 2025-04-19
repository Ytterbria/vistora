package com.ytterbria.vistorabackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureReviewRequest implements Serializable {

    private static final long serialVersionUID = 3057501806262291588L;

    /**
     * id
     */
    private Long id;

    /**
     * 状态: 0-待审核 1-审核通过 2-审核不通过
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

}
