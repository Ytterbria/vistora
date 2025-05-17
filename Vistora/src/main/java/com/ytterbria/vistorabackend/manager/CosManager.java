package com.ytterbria.vistorabackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.ytterbria.vistorabackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ytterbria
 * 通用的COS文件操作类
 */
@Component
@Slf4j
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

        // 1.图片压缩(转成webp格式)
        List<PicOperations.Rule> ruleList = new ArrayList<>();

        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        ruleList.add(compressRule);

        //2.缩略图处理
        if (file.length() > 10 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s", 512, 512));
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            ruleList.add(thumbnailRule);
        }
        //将图片Operations对象添加到请求中,
        //由于上传了两个规则这里会上传两个图片,一个是压缩图,一个是webp格式的图片
        picOperations.setRules(ruleList);
        putPictureRequest.setPicOperations(picOperations);
        return cosClient.putObject(putPictureRequest);



    }

    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }


}
