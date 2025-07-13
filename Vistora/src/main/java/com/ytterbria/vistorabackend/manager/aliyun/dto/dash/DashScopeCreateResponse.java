package com.ytterbria.vistorabackend.manager.aliyun.dto.dash;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DashScopeCreateResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 4122301231588098377L;

    private Output output;

    @Data
    public static class Output {
        private String taskId;
        private String taskStatus;
    }
}
