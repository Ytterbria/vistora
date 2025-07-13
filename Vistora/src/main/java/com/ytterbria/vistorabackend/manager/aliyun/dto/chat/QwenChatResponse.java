package com.ytterbria.vistorabackend.manager.aliyun.dto.chat;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
/**
 * 千问大模型聊天响应DTO
 */
@Data
public class QwenChatResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应ID
     */
    private String id;

    /**
     * 响应对象类型
     */
    private String object;

    /**
     * 响应创建时间（时间戳）
     */
    private Integer created;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 回复选项列表
     */
    private List<Choice> choices;

    /**
     * token用量信息
     */
    private Usage usage;

    // Getters and Setters

    /**
     * 回复选项
     */
    @Data
    public static class Choice implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * 回复消息内容
         */
        private Message message;

        /**
         * 结束原因
         */
        private String finishReason;

        /**
         * 选项索引
         */
        private Integer index;

        // Getters and Setters
    }

    /**
     * 消息内容
     */
    @Data
    public static class Message implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * 消息角色
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        // Getters and Setters
    }

    /**
     * token用量统计
     */
    @Data
    public static class Usage implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * 提示词token数
         */
        private Integer promptTokens;

        /**
         * 补全token数
         */
        private Integer completionTokens;

        /**
         * 总token数
         */
        private Integer totalTokens;

        // Getters and Setters
    }
}
