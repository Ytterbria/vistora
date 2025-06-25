package com.ytterbria.vistorabackend.model.dto.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceUserAnalyzeResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -2379540144550089336L;

    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;
}
