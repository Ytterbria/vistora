package com.ytterbria.vistorabackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ytterbria.vistorabackend.common.exception.BusinessException;
import com.ytterbria.vistorabackend.common.exception.ErrorCode;
import com.ytterbria.vistorabackend.common.exception.ThrowUtils;
import com.ytterbria.vistorabackend.mapper.PictureMapper;
import com.ytterbria.vistorabackend.mapper.SpaceMapper;
import com.ytterbria.vistorabackend.model.dto.space.analyze.*;
import com.ytterbria.vistorabackend.model.entity.Picture;
import com.ytterbria.vistorabackend.model.entity.Space;
import com.ytterbria.vistorabackend.model.entity.User;
import com.ytterbria.vistorabackend.service.PictureService;
import com.ytterbria.vistorabackend.service.SpaceAnalyzeService;
import com.ytterbria.vistorabackend.service.SpaceService;
import com.ytterbria.vistorabackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;

    @Resource
    private PictureMapper pictureMapper;

    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper){
        if (spaceAnalyzeRequest.getQueryAll()){
            return;
        }
        if (spaceAnalyzeRequest.getQueryPub()){
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null){
            queryWrapper.eq("spaceId",spaceId);
            return ;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"未指定查询范围");
    }
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest request, User loginUser){
        if (request.getQueryAll() ||  request.getQueryPub()){
            // 查询公共图库或全空间时需要管理员权限
            ThrowUtils.throwIf(!userService.isAdmin(loginUser),ErrorCode.NO_AUTH_ERROR);
        }else {
            Long spaceId = request.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <=0, ErrorCode.PARAMS_ERROR, "空间ID不存在");
            Space space = this.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space),ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(space, loginUser);
        }
    }

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest request,User loginUser){
        ThrowUtils.throwIf(ObjUtil.isNull(request),ErrorCode.PARAMS_ERROR);
        if (request.getQueryAll() || request.getQueryPub()){
            //查询公共图库或全空间时需要管理员权限
            ThrowUtils.throwIf(!userService.isAdmin(loginUser),ErrorCode.NO_AUTH_ERROR,"查询公共图库或全空间需要管理员权限");

            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            if (!request.getQueryAll()){
                queryWrapper.isNull("spaceId");
            }
            /*
                * 查询公共图库或全空间时，查询所有图片的大小
                * 只需要查询picSize字段
                * 所以不需要封装Picture类
                * 直接查询返回Object对象即可,提升性能并节省空间
             */
            List<Object> pictureObjectList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            Long usedSize = pictureObjectList.stream()
                    .filter(ObjUtil::isNotNull)
                    .mapToLong(res -> res instanceof Long ? (Long)res : 0L)
                    .sum();
            long usedCount = pictureObjectList.size();
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            //公共图库没有上限限制和比例
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            //查询指定空间
            Long spaceId = request.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0,ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjUtil.isNull(space),ErrorCode.NOT_FOUND_ERROR,"空间不存在");

            spaceService.checkSpaceAuth(space,loginUser);

            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getSpaceSize());
            response.setMaxSize(space.getMaxSize());
            Double sizeUsageRatio = NumberUtil.round(space.getSpaceSize() * 100.0 / space.getMaxSize(),2).doubleValue();
            response.setSizeUsageRatio(sizeUsageRatio);
            response.setUsedCount(space.getSpaceCount());
            response.setMaxCount(space.getMaxCount());
            Double countUsageRatio = NumberUtil.round(space.getSpaceCount() * 100.0 / space.getMaxCount(),2).doubleValue();
            response.setCountUsageRatio(countUsageRatio);
            return response;
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest request,User loginUser){
        ThrowUtils.throwIf(ObjUtil.isNull(request),ErrorCode.PARAMS_ERROR);

        // 检查权限
        checkSpaceAnalyzeAuth(request,loginUser);

        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(request,queryWrapper);

        queryWrapper.select("category as category",
                "COUNT(*) AS count",
                "SUM(picSize) AS totalSize")
                    .groupBy("category");

        return pictureMapper.selectMaps(queryWrapper).stream()
                .map(res -> {
                    String category = res.get("category") != null ? res.get("category").toString() : "未分类";
                    Long count = ((Number) res.get("count")).longValue();
                    Long totalSize = ((Number) res.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category,count,totalSize);
                }).toList();
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest request,User loginUser){
        ThrowUtils.throwIf(ObjUtil.isNull(request),ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(request,loginUser);

        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(request,queryWrapper);

        //查询图片标签统计
        queryWrapper.select("tags");
        List<String> tagsList = pictureMapper.selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .toList();

        Map<String,Long> tagCountMap = tagsList.stream()
                .flatMap(tag -> JSONUtil.toList(tag,String.class).stream())
                .collect(Collectors.groupingBy(tag->tag,Collectors.counting()));

        return tagCountMap.entrySet().stream()
                .sorted((e1,e2) -> Long.compare(e2.getValue(),e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest request,User loginUser){
        ThrowUtils.throwIf(ObjUtil.isNull(request),ErrorCode.PARAMS_ERROR);

        //检查权限
        checkSpaceAnalyzeAuth(request,loginUser);

        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        Long userId = request.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId),"userId",userId);
        fillAnalyzeQueryWrapper(request,queryWrapper);

        //分析维度
        String timeDimension = request.getTimeDimension();
        switch(timeDimension){
            case "day" -> queryWrapper.select("DATE_FORMAT(createTime,'%Y-%m-%d') AS period,COUNT(*) AS count");
            case "week" -> queryWrapper.select("YEARWEEK(createTime) AS period","COUNT(*) AS count");
            case "month" -> queryWrapper.select("MONTH(createTime) AS period","COUNT(*) AS count");
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        queryWrapper.groupBy("period").orderByAsc("count");

        List<Map<String,Object>> queryResult = pictureMapper.selectMaps(queryWrapper);

        return queryResult.stream()
                .map(res ->{
                    String period = res.get("period").toString();
                    Long count = ((Number) res.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(period,count);
                }).toList();
    }
}
