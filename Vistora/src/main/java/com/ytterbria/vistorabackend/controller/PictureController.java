package com.ytterbria.vistorabackend.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ytterbria.vistorabackend.annotation.AuthCheck;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import com.ytterbria.vistorabackend.model.dto.picture.PictureEditRequest;
import com.ytterbria.vistorabackend.model.dto.picture.PictureQueryRequest;
import com.ytterbria.vistorabackend.model.dto.picture.PictureUpdateRequest;
import com.ytterbria.vistorabackend.model.dto.picture.PictureUploadRequest;
import com.ytterbria.vistorabackend.model.entity.Picture;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.PictureVO;
import com.ytterbria.vistorabackend.service.PictureService;
import com.ytterbria.vistorabackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    /**
     * 上传图片
     * @param multipartFile 用户上传的文件
     * @param pictureUploadRequest 图片上传请求参数
     * @param httpServletRequest http请求
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file")MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest httpServletRequest
            ){
        User loginUser = userService.getLoginUserInfo(httpServletRequest);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile,pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片 仅管理员和本人可用
     * @param deleteRequest 图片删除请求参数
     * @param httpServletRequest http请求
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(ObjUtil.isEmpty(deleteRequest) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUserInfo(httpServletRequest);
        //判断将要删除的图片是否存在
        long id = deleteRequest.getId();
        Picture pictureToDelete = pictureService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureToDelete), ErrorCode.NOT_FOUND_ERROR);

        //判断用户是否有权限删除图片,仅本人或管理员可以删除
        ThrowUtils.throwIf(!pictureToDelete.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);

        //操作数据库
        boolean result = pictureService.removeById(pictureToDelete.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
     }

     /**
      * 更新图片 仅管理员可用
      * @param pictureUpdateRequest 图片更新请求参数
      */
     @PostMapping("/update")
     @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest){
         ThrowUtils.throwIf(ObjUtil.isEmpty(pictureUpdateRequest) || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

         //数据转换
         Picture picture = new Picture();
         BeanUtils.copyProperties(pictureUpdateRequest,picture);
         picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

         //数据校验
         pictureService.validatePicture(picture);

         //判断是否存在
         long id = pictureUpdateRequest.getId();
         Picture pictureToUpdate = pictureService.getById(id);
         ThrowUtils.throwIf(ObjUtil.isEmpty(pictureToUpdate), ErrorCode.NOT_FOUND_ERROR);

         //操作数据库
         boolean result = pictureService.updateById(picture);
         ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
         return ResultUtils.success(true);
     }

    /**
     *  根据id获取图片 仅管理员可用
     */
     @GetMapping("/get")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Picture> getPictureById(long id,HttpServletRequest httpServletRequest){
         ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR);
         //查询数据库
         Picture picture = pictureService.getById(id);
         ThrowUtils.throwIf(ObjUtil.isEmpty(picture), ErrorCode.NOT_FOUND_ERROR);
         return ResultUtils.success(picture);
     }

     /**
      *  根据id获取图片VO
      */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id,HttpServletRequest httpServletRequest){
         ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR);
         //查询数据库
         Picture picture = pictureService.getById(id);
         ThrowUtils.throwIf(ObjUtil.isEmpty(picture), ErrorCode.NOT_FOUND_ERROR);
         //数据转换
         PictureVO pictureVO = pictureService.getPictureVO(picture,httpServletRequest);
         return ResultUtils.success(pictureVO);
    }

    /**
     *  根据条件查询图片列表类 仅管理员可用
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest httpServletRequest){
         long current = pictureQueryRequest.getCurrent();
         long size = pictureQueryRequest.getPageSize();
         ThrowUtils.throwIf(current <= 0 || size <= 0, ErrorCode.PARAMS_ERROR);

         Page<Picture> picturePage = pictureService.page(new Page<>(current,size),pictureService.getQueryWrapper(pictureQueryRequest));
         return ResultUtils.success(picturePage);
    }

    /**
     *  根据条件查询图片列表VO 仅管理员可用
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest httpServletRequest){
         long current = pictureQueryRequest.getCurrent();
         long size = pictureQueryRequest.getPageSize();
         ThrowUtils.throwIf(current <= 0 || size <= 0 || size >= 20, ErrorCode.PARAMS_ERROR);

         Page<Picture> picturePage = pictureService.page(new Page<>(current,size),pictureService.getQueryWrapper(pictureQueryRequest));
         return ResultUtils.success(pictureService.getPictureVOPage(picturePage,httpServletRequest));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,HttpServletRequest httpServletRequest){
         ThrowUtils.throwIf(ObjUtil.isEmpty(pictureEditRequest) || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

         //数据转换
         Picture picture = new Picture();
         BeanUtils.copyProperties(pictureEditRequest,picture);
         picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
         picture.setEditTime(new Date());

         //数据校验
         pictureService.validatePicture(picture);
         User loginUser = userService.getLoginUserInfo(httpServletRequest);
         long id = pictureEditRequest.getId();
         Picture pictureToEdit = pictureService.getById(id);
         ThrowUtils.throwIf(ObjUtil.isEmpty(pictureToEdit), ErrorCode.NOT_FOUND_ERROR);
         ThrowUtils.throwIf(!pictureToEdit.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);

         //操作数据库
         boolean result = pictureService.updateById(picture);
         ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
         return ResultUtils.success(true);
    }
}
