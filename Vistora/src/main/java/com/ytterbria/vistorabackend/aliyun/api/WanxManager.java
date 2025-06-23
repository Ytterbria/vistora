package com.ytterbria.vistorabackend.aliyun.api;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ytterbria.vistorabackend.aliyun.dto.dash.DashScopeCreateRequest;
import com.ytterbria.vistorabackend.aliyun.dto.dash.DashScopeCreateResponse;
import com.ytterbria.vistorabackend.aliyun.dto.dash.DashScopeTaskResultResponse;
import com.ytterbria.vistorabackend.aliyun.dto.pic.CreateImageTaskRequest;
import com.ytterbria.vistorabackend.aliyun.dto.pic.CreateImageTaskResponse;
import com.ytterbria.vistorabackend.aliyun.dto.pic.QueryImageTaskResponse;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WanxManager {
    @Value("${tongyi.wanx.secret_key}")
    private String secretKey;

    @Value("${tongyi.wanx.model_name}")
    private String model;

    private static final String CREATE_IMAGE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    private static final String QUERY_IMAGE_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/";

    public CreateImageTaskResponse createImageTask(CreateImageTaskRequest request){
        //构建图片创建请求,传入用户输入的提示词和模型名称
        DashScopeCreateRequest dashRequest = new DashScopeCreateRequest(request.getPrompt());
        dashRequest.setModel(model);

        // 通过hutool发起HTTP请求调用api
        HttpRequest httpRequest = HttpRequest.post(CREATE_IMAGE_URL)
                .header("Authorization","Bearer " + secretKey)
                .header("X-DashScope-Async","enable")
                .body(JSONUtil.toJsonStr(dashRequest));

        try(HttpResponse httpResponse = httpRequest.execute()){

            ThrowUtils.throwIf(!httpResponse.isOk(), ErrorCode.OPERATION_ERROR,"调用AI响应失败: " + httpResponse.body());

            DashScopeCreateResponse dashResponse = JSONUtil.toBean(httpResponse.body(),DashScopeCreateResponse.class);
            String taskId = dashResponse.getOutput().getTaskId();
            String taskStatus = dashResponse.getOutput().getTaskStatus();
            ThrowUtils.throwIf(ObjUtil.isNull(taskId),ErrorCode.OPERATION_ERROR,"AI任务响应为空");
            return new CreateImageTaskResponse(taskId, taskStatus);
        }
    }

    public QueryImageTaskResponse queryTask(String taskId){
        //构建查询任务请求
        String url = QUERY_IMAGE_URL + taskId;

        // 通过hutool发起HTTP请求调用api
        HttpRequest httpRequest = HttpRequest.get(url)
                .header("Authorization","Bearer " + secretKey);

        try(HttpResponse httpResponse = httpRequest.execute()){

            ThrowUtils.throwIf(!httpResponse.isOk(), ErrorCode.OPERATION_ERROR,"调用AI响应失败: " + httpResponse.body());

            DashScopeTaskResultResponse dashResponse = JSONUtil.toBean(httpResponse.body(),DashScopeTaskResultResponse.class);
            QueryImageTaskResponse queryResponse = new QueryImageTaskResponse();
            queryResponse.setTaskId(dashResponse.getOutput().getTaskId());
            if ("SUCCEEDED".equals(dashResponse.getOutput().getTaskStatus())){
                log.info("AI任务查询成功，任务ID: {}", taskId );
                log.info("获得的urls: {}", dashResponse.getOutput().getResults());
                dashResponse.getOutput().getResults().forEach(result ->{
                    queryResponse.getImageUrls().add(result.getUrl());
                });
            } else {
                queryResponse.setStatus(dashResponse.getOutput().getTaskStatus());
            }
            return queryResponse;
        }
    }
}
