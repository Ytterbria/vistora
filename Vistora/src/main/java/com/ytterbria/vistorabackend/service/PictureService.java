package com.ytterbria.vistorabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ytterbria.vistorabackend.model.dto.picture.*;
import com.ytterbria.vistorabackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author lenovo
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-27 15:53:50
*/
public interface PictureService extends IService<Picture> {

    /**
    * @description 上传图片
    * @param inputSource 图片输入源
    * @param pictureUploadRequest 图片上传请求
    * @param loginUser 登录用户
    * @return PictureVO 封装的图片信息
    */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * @description 批量抓取图片
     * @param pictureUploadByBatchRequest 批量抓取请求
     * @param loginUser 登录用户
     * @return 上传成功的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,User loginUser);

    /**
     * @description 构造图片QueryWrapper
     * @param pictureQueryRequest 图片上传请求
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * @description 获得单个图片封装类
     * @param picture 图片实体类
     * @param request http请求
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * @description 获得分页图片封装类
     * @param picturePage 分页图片实体类
     * @param request http请求
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage,HttpServletRequest request);

    boolean editPicture(PictureEditRequest pictureEditRequest,HttpServletRequest request);

    boolean updatePicture(PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request);

    void validatePicture(Picture picture);

    void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture,User loginUser);
}
