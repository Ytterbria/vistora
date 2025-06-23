package com.ytterbria.vistorabackend.aliyun.dto.chat;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
/**
 * 表示向千问大模型发起聊天请求的DTO
 */
@Data
public class QwenChatRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 聊天消息列表
     */
    private List<Message> messages;

    /**
     * 是否以流式方式返回结果
     */
    private boolean stream;

    /**
     * 生成回复的最大token数
     */
    private Integer maxTokens;

    /**
     * 采样温度，控制输出的随机性
     */
    private Double temperature;

    /**
     * nucleus采样的阈值
     */
    private Double topP;

    /**
     * 生成停止的标志字符串
     */
    private String stop;

    /**
     * 生成的回复数量
     */
    private Integer n;

    // Getters and Setters

    /**
     * 聊天消息对象
     */
    @Data
    public static class Message implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * 消息角色，如"user"或"assistant"
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        // Getters and Setters

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        public Message(String role) {
            this.role = role;
        }
    }
}
