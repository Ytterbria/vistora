package com.ytterbria.vistorabackend.mapper;

import com.ytterbria.vistorabackend.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ytterbria.vistorabackend.model.entity.Space;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author lenovo
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-03-27 15:53:50
* @Entity com.ytterbria.vistorabackend.model.entity.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {

    @Results({
            @Result(property = "category", column = "category"),
            @Result(property = "count", column = "count"),
            @Result(property = "totalSize", column = "totalSize")
    })
    @Select("SELECT category as category,COUNT(*) as count from picture group by category")
    List<Map<String, Object>> getCategoryAnalyze();
}




