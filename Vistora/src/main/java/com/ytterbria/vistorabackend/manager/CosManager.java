package com.ytterbria.vistorabackend.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.ytterbria.vistorabackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author ytterbria
 * 通用的COS文件操作类
 */
@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件到cos
     * @param key  对象键,对象在存储桶中的唯一标识,可以用类似路径的形式表示,如"path/to/file"
     * @param file 上传的文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(),key,file);

        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 从cos下载对象
     * @param key  对象键
     * @return 下载结果
     */
    public COSObject getObject(String key){
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(),key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传图片,并且返回万象解析图片过后的相关信息
     */
    public PutObjectResult putPictureObject(String key, File file){
        //构造上传对象请求,设置bucket,key,文件
        PutObjectRequest putPictureRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        //利用腾讯万象对图片进行处理
        PicOperations picOperations = new PicOperations();
        // 1表示开启图片信息返回
        picOperations.setIsPicInfo(1);
        //将图片Operations对象添加到请求中
        putPictureRequest.setPicOperations(picOperations);
        return cosClient.putObject(putPictureRequest);
    }


}
