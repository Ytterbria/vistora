package com.ytterbria.vistorabackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.config.CosClientConfig;
import com.ytterbria.vistorabackend.manager.CosManager;
import com.ytterbria.vistorabackend.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
            //获得处理后的图片结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            ThrowUtils.throwIf(CollUtil.isEmpty(objectList),ErrorCode.OPERATION_ERROR,"图片处理失败");

            //获取压缩之后得到的文件信息
            CIObject compressedCiObject = objectList.get(0);
            CIObject thumbnailCiObject = objectList.get(0);
            if (objectList.size() > 1) {
                thumbnailCiObject = objectList.get(1);
            }
            //4.封装返回结果
            return wrapUploadPictureResult(originFilename,compressedCiObject,thumbnailCiObject);
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
     * @param ciObject 处理后的图片结果
     * @return 上传结果
     */
    private UploadPictureResult wrapUploadPictureResult(String originFilename, CIObject ciObject,  CIObject thumbnailCiObject){
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = ciObject.getWidth();
        int picHeight = ciObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight,2).doubleValue();
        uploadPictureResult.setPicName(originFilename);
        uploadPictureResult.setPicSize(ciObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(ciObject.getFormat());
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + ciObject.getKey());
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
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
