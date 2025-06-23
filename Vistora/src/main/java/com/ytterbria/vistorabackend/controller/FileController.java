package com.ytterbria.vistorabackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.ytterbria.vistorabackend.annotation.AuthCheck;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.manager.CosManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    @Resource
    CosManager cosManager;

    @GetMapping("/download")
    @AuthCheck(mustRole="admin")
    public void testDownloadFile(String key, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try{
            //从cos中获得文件对象
            COSObject cosObject = cosManager.getObject(key);
            //从文件对象中获得输入流
            cosObjectInput = cosObject.getObjectContent();
            //将输入流转换为字节数组
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            //响应内容: octet-stream(二进制流)
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Dispposition","attachment;filename=" + key);

            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error,filepath = " + key,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文件下载失败");
        }finally{
            if (cosObjectInput != null){
                cosObjectInput.close();
            }
        }
    }
}
