package com.ytterbria.vistorabackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Data;
import lombok.Getter;

@Getter
public enum SpaceLevelEnum {

    COMMON("普通版", 0, 240, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 2000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 20000, 10000L * 1024 * 1024);

    private final String text;

    private final int value;

    private final long maxCount;

    private final long maxSize;

    SpaceLevelEnum(String text,int value,long maxCount,long maxSize){
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    public static SpaceLevelEnum getEnumByValue(Integer value){
        if (ObjUtil.isEmpty(value)){
            return null;
        }

        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()){
            if (spaceLevelEnum.getValue() == value){
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
