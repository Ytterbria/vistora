package com.ytterbria.vistorabackend.manager.aliyun.dto.dash;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class DashScopeTaskResultResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 7981876500477709116L;

    private Output output;

    @Data
    public static class Output {
        private String taskId;
        private String taskStatus;
        private List<Result> results;
    }

    @Data
    public static class Result {
        private String url;
    }
}
