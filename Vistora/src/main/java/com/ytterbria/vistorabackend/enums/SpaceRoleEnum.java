package com.ytterbria.vistorabackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SpaceRoleEnum {
    VIEWER("浏览者","viewer"),
    EDITOR("编辑者","editor"),
    ADMIN("管理员","admin");

    private final String text;

    private final String value;

    SpaceRoleEnum(String text,String value){
        this.text = text;
        this.value = value;
    }

    public static SpaceRoleEnum getEnumByValue(String value){
        if (ObjUtil.isNull(value)){
            return null;
        }
        for (SpaceRoleEnum spaceRoleEnum : SpaceRoleEnum.values()){
            if (spaceRoleEnum.getValue().equals(value)){
                return spaceRoleEnum;
            }
        }
        return null;
    }

    public static List<String> getAllTexts(){
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getText)
                .collect(Collectors.toList());
    }
}
