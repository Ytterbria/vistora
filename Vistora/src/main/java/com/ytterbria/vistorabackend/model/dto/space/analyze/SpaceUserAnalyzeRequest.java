package com.ytterbria.vistorabackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8132905136731146364L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 时间维度 day/week/month
     */
    private String timeDimension;

}
