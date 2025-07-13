package com.ytterbria.vistorabackend.manager.aliyun.api;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ytterbria.vistorabackend.manager.aliyun.dto.chat.QwenChatRequest;
import com.ytterbria.vistorabackend.manager.aliyun.dto.chat.QwenChatResponse;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class TongyiQwen {
    //密钥
    @Value("${tongyi.qwen.secret_key}")
    private String secretKey;
    //模型名称
    @Value("${tongyi.qwen.model_name}")
    private String model;

    public String chatQwen(String userInput){
        QwenChatRequest request = new QwenChatRequest();
        // 填入默认参数
        request.setModel(model);
        request.setMessages(Arrays.asList(new QwenChatRequest.Message("user", userInput),
                        new QwenChatRequest.Message("assistant",
                                "你的任务是回答用户的问题，提供有用的信息和建议。" +
                                        "请确保你的回答准确、清晰，并且符合用户的需求。")));
        //发起chat请求
        String QWEN_CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        HttpRequest httpRequest = HttpRequest.post(QWEN_CHAT_URL)
                .header("Authorization","Bearer " + secretKey)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(request));
        // 使用try-with-resources来自动关闭HttpResponse
        try (HttpResponse response = httpRequest.execute()) {
            if (!response.isOk()){
                log.error("请求失败,状态码: {}, 响应: {}", response.getStatus(), response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"Qwen Chat API 请求失败，请稍后再试");
            }
            // 解析响应
            QwenChatResponse chatResponse = JSONUtil.toBean(response.body(), QwenChatResponse.class);
            if (ObjUtil.isNotEmpty(chatResponse.getChoices())){
                return chatResponse.getChoices().get(0).getMessage().getContent();
            } else {
                log.error("没有返回有效的回答");
                return "没有返回有效的回答，请稍后再试。";
            }
        }
    }


}

