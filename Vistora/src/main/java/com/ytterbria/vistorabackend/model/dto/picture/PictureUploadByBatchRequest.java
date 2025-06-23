package com.ytterbria.vistorabackend.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class PictureUploadByBatchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 328148260680021411L;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取的数量
     */
    private Integer count = 10;
}
