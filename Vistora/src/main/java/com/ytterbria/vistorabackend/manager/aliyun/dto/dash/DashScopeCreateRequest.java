package com.ytterbria.vistorabackend.manager.aliyun.dto.dash;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashScopeCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2440938905467801516L;

    private String model;
    private Input input;
    private Map<String, Object> parameters;

    @Data
    @AllArgsConstructor
    public static class Input {
        private String prompt;
    }

    public DashScopeCreateRequest(String prompt) {
        this.input = new Input(prompt);
        this.parameters = Map.of("size", "1024*1024", "n", 1);
    }
}
