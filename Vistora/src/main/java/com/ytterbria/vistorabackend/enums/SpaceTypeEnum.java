package com.ytterbria.vistorabackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum SpaceTypeEnum {

    PRIVATE_SPACE("私人空间",0),
    TEAM_SPACE("公共空间",1);

    private final String text;

    private final int value;

    SpaceTypeEnum(String text,int value){
        this.text = text;
        this.value=  value;
    }

    public static SpaceTypeEnum getEnumByValue(Integer value){
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()){
            if (spaceTypeEnum.getValue() == value){
                return spaceTypeEnum;
            }
        }
        return null;
    }


}
