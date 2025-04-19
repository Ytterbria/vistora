package com.ytterbria.vistorabackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.constant.PictureConstant;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR,"文件不能为空");
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 10 * ONE_MB, ErrorCode.PARAMS_ERROR,"文件大小不能超过10MB");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!PictureConstant.ALLOWED_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR,"文件格式错误");
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
