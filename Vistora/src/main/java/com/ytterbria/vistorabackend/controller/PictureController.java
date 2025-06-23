package com.ytterbria.vistorabackend.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ytterbria.vistorabackend.annotation.AuthCheck;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.common.request.DeleteRequest;
import com.ytterbria.vistorabackend.common.request.PageRequest;
import com.ytterbria.vistorabackend.common.response.BaseResponse;
import com.ytterbria.vistorabackend.common.response.ResultUtils;
import com.ytterbria.vistorabackend.config.CosClientConfig;
import com.ytterbria.vistorabackend.constant.UserConstant;
import com.ytterbria.vistorabackend.enums.PictureReviewEnum;
import com.ytterbria.vistorabackend.manager.CosManager;
import com.ytterbria.vistorabackend.model.dto.picture.*;
import com.ytterbria.vistorabackend.model.entity.Picture;
import com.ytterbria.vistorabackend.model.entity.Space;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.model.vo.PictureTagCategory;
import com.ytterbria.vistorabackend.model.vo.PictureVO;
import com.ytterbria.vistorabackend.service.PictureService;
import com.ytterbria.vistorabackend.service.SpaceService;
import com.ytterbria.vistorabackend.service.UserService;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存
     */
    private final Cache<String,String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)//初始化缓存数量
            .maximumSize(10_000L)//最大缓存数量
            .expireAfterWrite(Duration.ofMinutes(5))//过期时间为5分钟
            .build();
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
        ThrowUtils.throwIf(ObjUtil.isEmpty(multipartFile) || ObjUtil.isEmpty(pictureUploadRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserInfo(httpServletRequest);

        PictureVO pictureVO = pictureService.uploadPicture(multipartFile,pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest httpServletRequest
            ){
        // 校验参数
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureUploadRequest), ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUserInfo(httpServletRequest);
         String fileUrl = pictureUploadRequest.getFileUrl();
         PictureVO pictureVO = pictureService.uploadPicture(fileUrl,pictureUploadRequest,loginUser);
         return ResultUtils.success(pictureVO);
    }

    /**
     *  批量上传图片
     * @param pictureUploadByBatchRequest 图片批量上传请求参数
     * @param httpServletRequest http请求
     * @return 上传成功的图片数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole="admin")
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureUploadByBatchRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserInfo(httpServletRequest);
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest,loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 删除图片 仅管理员和本人可用
     * @param deleteRequest 图片删除请求参数
     * @param httpServletRequest http请求
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest httpServletRequest){

        ThrowUtils.throwIf(ObjUtil.isEmpty(deleteRequest), ErrorCode.PARAMS_ERROR);

        boolean result = pictureService.deletePicture(deleteRequest,httpServletRequest);

       return ResultUtils.success(result);
     }

     /**
      * 更新图片 仅管理员可用
      * @param pictureUpdateRequest 图片更新请求参数
      */
     @PostMapping("/update")
     @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest httpServletRequest){

         ThrowUtils.throwIf(ObjUtil.isEmpty(pictureUpdateRequest) || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

         Boolean result = pictureService.updatePicture(pictureUpdateRequest,httpServletRequest);

         return ResultUtils.success(result);
     }

    /**
     *  根据id获取图片 仅管理员可用
     */
     @GetMapping("/get")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Picture> getPictureById(long id){
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
     *  根据条件查询图片列表VO
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest httpServletRequest){
         int current = pictureQueryRequest.getCurrent();
         int size = pictureQueryRequest.getPageSize();
         ThrowUtils.throwIf(current <= 0 || size <= 0 || size >= 20, ErrorCode.PARAMS_ERROR);

         pictureQueryRequest.setReviewStatus(PictureReviewEnum.PASS.getValue());
         pictureQueryRequest.setCurrent(current);
         pictureQueryRequest.setPageSize(size);

        //空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUserInfo(httpServletRequest);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有权限访问该空间");
        } else {
            pictureQueryRequest.setReviewStatus(PictureReviewEnum.PASS.getValue());
            pictureQueryRequest.setPubOnly(1);
        }

         Page<Picture> picturePage = pictureService.page(new Page<>(current,size),pictureService.getQueryWrapper(pictureQueryRequest));
         return ResultUtils.success(pictureService.getPictureVOPage(picturePage));
    }

    @PostMapping("/list/page/vo/cache")
    @Deprecated
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest httpServletRequest){
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || size <= 0 || size >= 20, ErrorCode.PARAMS_ERROR);

        pictureQueryRequest.setReviewStatus(PictureReviewEnum.PASS.getValue());
        pictureQueryRequest.setCurrent(current);
        pictureQueryRequest.setPageSize(size);
        //空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUserInfo(httpServletRequest);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有权限访问该空间");
        } else {
            pictureQueryRequest.setReviewStatus(PictureReviewEnum.PASS.getValue());
            pictureQueryRequest.setPubOnly(1);
        }
        //构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "vistora:ListPictureVOByPageWithCache:" + hashKey;
        //1.先查本地缓存caffeine中有没有记录
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);

        if(cachedValue != null){
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        //2.如果本地缓存没有查到,那就再查redis缓存
        ValueOperations<String,String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null){
            LOCAL_CACHE.put(cacheKey,cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue,Page.class);
            return ResultUtils.success(cachedPage);
        }

        //3.如果redis缓存也没有查到,那就查数据库,然后存入redis缓存
        Page<Picture> picturePage = pictureService.page(new Page<>(current,size),pictureService.getQueryWrapper(pictureQueryRequest));
        String cacheValue = JSONUtil.toJsonStr(pictureService.getPictureVOPage(picturePage));
        int cacheExpireSeconds = 200 + RandomUtil.randomInt(100, 300);//随机过期时间，避免缓存雪崩
        valueOps.set(cacheKey,cacheValue,cacheExpireSeconds);

        //返回结果
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage));
    }

    /**
     * 处理图片编辑请求 给普通用户用来编辑图片
     *
     * @param pictureEditRequest 包含图片编辑信息的请求对象，包括图片ID、标题、描述等
     * @param httpServletRequest HTTP请求对象，用于获取请求相关的信息
     * @return 返回一个包含布尔值的响应对象，表示图片是否编辑成功
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,HttpServletRequest httpServletRequest){
         ThrowUtils.throwIf(ObjUtil.isEmpty(pictureEditRequest) || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

         return ResultUtils.success(pictureService.editPicture(pictureEditRequest,httpServletRequest));
    }

    /**
     * 批量编辑图片
     * 该方法用于批量编辑图片信息，仅限于管理员角色的用户使用
     *
     * @param pictureEditByBatchRequest 包含批量编辑所需信息的请求对象，如图片ID列表和编辑内容
     * @param httpServletRequest        HTTP请求对象，用于获取当前登录用户信息
     * @return 返回一个表示操作成功的布尔值，true表示成功
     */
    @PostMapping("/edit/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,
                                                    HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(pictureEditByBatchRequest), ErrorCode.PARAMS_ERROR);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, httpServletRequest);
        return ResultUtils.success(true);
    }

    /**
     * 处理图片审核请求
     * 该方法用于对上传的图片进行审核操作，仅限于管理员角色的用户使用
     *
     * @param pictureReviewRequest 包含审核所需信息的请求对象，如图片ID和审核结果
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 返回一个表示操作成功的布尔值，true表示成功
     *
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserInfo(request);
        pictureService.reviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }


    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意","风景");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

}
