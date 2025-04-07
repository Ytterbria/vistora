package com.ytterbria.vistorabackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//作用范围: 方法
@Retention(RetentionPolicy.RUNTIME)// 生命周期
public @interface AuthCheck {//@interface : 这个注解是一个元注解，可以用来注解其他注解。
    //必须有某个角色
    String mustRole() default "";//这个注解有一个属性，必须有某个角色，默认为空字符串
}
