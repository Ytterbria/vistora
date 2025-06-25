package com.ytterbria.vistorabackend.model.dto.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 2367947030059402086L;

    /**
     * 标签名
     */
    private String tag;

    /**
     * 使用次数
     */
    private Long count;


}
