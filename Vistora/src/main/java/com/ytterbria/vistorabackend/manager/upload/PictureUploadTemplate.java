package com.ytterbria.vistorabackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.config.CosClientConfig;
import com.ytterbria.vistorabackend.manager.CosManager;
import com.ytterbria.vistorabackend.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix){
        //1.校验文件
        validatePicture(inputSource);

        //2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("%s/%s",uploadPathPrefix,uploadFilename);

        //3.临时文件创建
        File file = null;
        try{
            file = File.createTempFile(uploadPath,null);
            processFile(inputSource,file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath,file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            //4.封装返回结果
            return wrapUploadPictureResult(originFilename,file,uploadPath,imageInfo);
        } catch(Exception e){
            log.error("图片上传到cos失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片上传到cos失败");
        }finally{
            deleteTempFile(file);
        }



    }

    /**
     * 校验输入源(本地文件或者url)
     * @param inputSource 输入源
     */
    protected abstract void validatePicture(Object inputSource);

    /**
     * 获取原始文件名
     * @param inputSource 输入源
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入原并且生成本地临时文件
      * @param inputSource 输入源
     * @param file 本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file ) throws Exception;

    /**
     * 封装返回结果
     * @param originFilename 原始文件名
     * @param file 本地临时文件
     * @param uploadPath 上传路径
     * @param imageInfo 图片信息
     */
    private UploadPictureResult wrapUploadPictureResult(String originFilename, File file,String uploadPath, ImageInfo imageInfo){
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight,2).doubleValue();
        uploadPictureResult.setPicName(originFilename);
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        return uploadPictureResult;
    }
    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file){
        if (file == null){
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult){
            log.error("file delete error, file path => {}",file.getAbsolutePath());
        }
    }
}
