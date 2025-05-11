package com.ytterbria.vistorabackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.enums.PictureReviewEnum;
import com.ytterbria.vistorabackend.manager.PictureManager;
import com.ytterbria.vistorabackend.manager.upload.FilePictureUpload;
import com.ytterbria.vistorabackend.manager.upload.PictureUploadTemplate;
import com.ytterbria.vistorabackend.manager.upload.UrlPictureUpload;
import com.ytterbria.vistorabackend.model.dto.picture.*;
import com.ytterbria.vistorabackend.model.entity.Picture;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.LoginUserVO;
import com.ytterbria.vistorabackend.model.vo.PictureVO;
import com.ytterbria.vistorabackend.service.PictureService;
import com.ytterbria.vistorabackend.mapper.PictureMapper;
import com.ytterbria.vistorabackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author lenovo
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-27 15:53:50
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private FilePictureUpload filePictureUpload;

    /**
     * 上传图片 ,根据用户id划分目录,上传到cos的路径格式为: public/userId/上传时间_uuid.后缀
     *
     * @param inputSource          图片输入源 可以直接输入url或者传入图片文件
     * @param pictureUploadRequest 图片上传请求DTO
     * @param loginUser            登录用户
     * @return 上传结果DTO
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //upload一个图片的操作,有可能是新增,也有可能是更新图片,所以要校验是否已经存在图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        //上传图片,根据用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());

        // 根据inputSource类型,选择上传策略
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }

        //最后,上传到cos的路径格式为: public/userId/上传时间_uuid.后缀
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        
        //构造要存入图库的信息
        Picture picture = buildPictureResult(loginUser, uploadPictureResult, pictureId);
        this.fillReviewParams(picture,loginUser);

        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        int uploadCount = 0;
        //参数校验
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30,ErrorCode.PARAMS_ERROR,"一次最多上传30张图片");
        //处理抓取地址
        String resourceUrl = String.format("https://pixabay.com/api/?key=49885606-6f49f0bbe707c69885c43b48d&lang=zh&q=%s&per_page=%s",searchText,count);
        try{
            List<String> pictureUrls = fetchImageUrls(resourceUrl);
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            for (String pictureUrl : pictureUrls){
                PictureVO pictureVO = this.uploadPicture(pictureUrl,pictureUploadRequest,loginUser);
                uploadCount ++;
                log.info("图片上传成功,id:{}",pictureVO.getId());
            }
            return uploadCount;
        }catch(Exception e){
            log.error("图片抓取失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图片抓取失败");
        }
    }

    private List<String> fetchImageUrls(String resourceUrl){
        List<String> pictureToUploadUrls = new ArrayList<>();
        String response = HttpUtil.get(resourceUrl);
        JSONObject jsonObject =JSONUtil.parseObj(response);
        ThrowUtils.throwIf(jsonObject.isEmpty(),ErrorCode.OPERATION_ERROR,"图片抓取失败-响应数据为空");
        JSONArray hits = jsonObject.getJSONArray("hits");

        //遍历results数组,得到每个图片的url
        for(int i = 0; i < hits.size();i ++){
            JSONObject hit = hits.getJSONObject(i);
            String largeImageURL = hit.getStr("largeImageURL");
            pictureToUploadUrls.add(largeImageURL);
        }
        return pictureToUploadUrls;
    }
    private static Picture buildPictureResult(User loginUser, UploadPictureResult uploadPictureResult, Long pictureId) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        return picture;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }

        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();




        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw
                    .like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(ObjUtil.isNotEmpty(name), "name", name);
        queryWrapper.like(ObjUtil.isNotEmpty(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus),"reviewStatus",reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        if (CollUtil.isNotEmpty(tags)){
            for (String tag : tags){
                queryWrapper.like("tags","\"" + tag + "\"");
            }
        }

        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0){
            User user = userService.getById(userId);
            LoginUserVO userVO = userService.getLoginUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(),picturePage.getSize(),picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)){
            return pictureVOPage;
        }
        //picture对象列表 -> pictureVO封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO :: objToVo)
                .collect(Collectors.toList());

        //关联查询用户信息
        Set<Long> userIdSet = pictureList.stream()
                .map(Picture :: getUserId)
                .collect(Collectors.toSet());
        Map<Long,List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User :: getId));

        //填充信息
        pictureVOList.forEach(pictureVO ->{
           Long userId = pictureVO.getUserId();
           User user = null;
           if (userIdUserListMap.containsKey(userId)){
               user= userIdUserListMap.get(userId).get(0);
           }
           pictureVO.setUser(userService.getLoginUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public boolean updatePicture(PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureUpdateRequest) || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        //数据转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest,picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        //数据校验
        this.validatePicture(picture);

        //填写审核信息
        this.fillReviewParams(picture,userService.getLoginUserInfo(request));

        //判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture pictureToUpdate = this.getById(id);
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureToUpdate), ErrorCode.NOT_FOUND_ERROR);


        //操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public boolean editPicture(PictureEditRequest pictureEditRequest,HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureEditRequest), ErrorCode.PARAMS_ERROR);

        //数据转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest,picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());

        //数据校验
        this.validatePicture(picture);
        User loginUser = userService.getLoginUserInfo(httpServletRequest);
        long id = pictureEditRequest.getId();
        Picture pictureToEdit = this.getById(id);
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureToEdit), ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!pictureToEdit.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);

        //补充审核信息
        this.fillReviewParams(picture,loginUser);

        //操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public void validatePicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewEnum reviewStatusEnum = PictureReviewEnum.getPictureReviewEnumByValue(reviewStatus);

        ThrowUtils.throwIf(
                        id == null || reviewStatusEnum == null ||
                        PictureReviewEnum.REVIEWING.equals(reviewStatusEnum),
                        ErrorCode.PARAMS_ERROR
                );
        //判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(ObjUtil.isEmpty(oldPicture), ErrorCode.NOT_FOUND_ERROR);

        //已经是该状态
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatus), ErrorCode.OPERATION_ERROR,"图片已审核");
        //更新审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest,updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)){
            picture.setReviewStatus(PictureReviewEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewEnum.REVIEWING.getValue());
        }
    }
}




