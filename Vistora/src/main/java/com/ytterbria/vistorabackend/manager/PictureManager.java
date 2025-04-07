package com.ytterbria.vistorabackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.config.CosClientConfig;
import com.ytterbria.vistorabackend.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 是对COS Manager的再次封装,专用于处理图片解析,上传,下载等操作
 */
@Slf4j
@Service
public class PictureManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    @Resource
    private CosManager cosManager;
    /**
     * 上传图片并且封装返回结果
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix){
        //1.校验文件
        validPicture(multipartFile);
        //2.图片上传的路径
        String uuid = RandomUtil.randomString(16);//用uuid防止文件名重复
        //获取原始文件名,由于后续需要用url来访问图片,所以最好自己设定图片url规则,防止用户上传的文件名出现不符合url规范的情况
        String originalFilename = multipartFile.getOriginalFilename();
        //按照: 上传时间_uuid.后缀 的格式命名图片
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(originalFilename));
        //用户还可以自己设置上传路径前缀,比如: /pictures/
        String uploadPath = String.format("/%s/%s", uploadPathPrefix,uploadFilename);

        //3.解析图片结果并且返回相应数据
        File file = null;
        try{
            file = File.createTempFile(uploadFilename,null);
            multipartFile.transferTo(file);
            //上传图片到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath,file);
            UploadPictureResult uploadPictureResult = wrapUploadPictureResult(putObjectResult);
            uploadPictureResult.setPicName(uploadFilename);
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            return uploadPictureResult;
        }catch (Exception e){
            log.error("图片上传失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片上传失败");
        }finally{
            //4.临时文件清理
            this.deleteTempFile(file);
        }

    }


    /**
     * 校验文件
     * @param: multipartFile
     */
    public void validPicture(MultipartFile multipartFile){
        //校验是否为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        //校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > 20 * 1024 * 1024,ErrorCode.PARAMS_ERROR,"文件大小不能超过20M");
        //校验文件后缀(格式)
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> ALLOWED_SUFFIX_LIST = Arrays.asList("jpg", "jpeg", "png", "gif");
        ThrowUtils.throwIf(!ALLOWED_SUFFIX_LIST.contains(fileSuffix),ErrorCode.PARAMS_ERROR,"文件类型错误");
    }

    /**
     * 封装上传图片过后,被万象解析的返回结果
     */
    public UploadPictureResult wrapUploadPictureResult(PutObjectResult putObjectResult){

        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight,2).doubleValue();
        //封装返回结果,但是文件名,file大小,url需要从context中获取
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file){
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult){
            log.error("删除临时文件失败:{}",file.getAbsolutePath());
        }
    }
}
