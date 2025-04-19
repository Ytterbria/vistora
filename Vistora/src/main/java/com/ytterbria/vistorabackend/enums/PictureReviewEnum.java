package com.ytterbria.vistorabackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PictureReviewEnum {
    REVIEWING("待审核",0),
        PASS("通过",1),
            REJECT("拒绝",2);

    private final String text;

    private final int value;

    PictureReviewEnum(String text,int value){
        this.text = text;
        this.value = value;
    }

    /**
     * 通过value获取枚举类
     */
    public static PictureReviewEnum getPictureReviewEnumByValue(Integer value){
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (PictureReviewEnum pictureReviewEnum : PictureReviewEnum.values()){
            if (pictureReviewEnum.value == value){
                return pictureReviewEnum;
            }
        }
        return null;
    }
}
