package com.ytterbria.vistorabackend.common.response;

import com.ytterbria.vistorabackend.common.exception.ErrorCode;

/**
 * 有关泛型和通配:
 * BaseResponse<T> <T extends Object> 泛型类，T可以是任意类型，但是只能是Object的子类
 * BaseResponse<?> 通配符类，可以接收任意类型，只能读取,但是不能设置T的值
 * 而对于success方法中的<T>, 则可以接收任意类型，但是只能是Object的子类,
 * 作用于方法的泛型,<T>写在返回值之前
 * 作用域类的泛型<T extends Object>写在类名之后
 */
public class ResultUtils {
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<> (ErrorCode.SUCCESS.getCode(), data,ErrorCode.SUCCESS.getMessage());
    }

    public static BaseResponse<?> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse<?> error(ErrorCode errorCode,String message){
        return new BaseResponse<>(errorCode.getCode(),message);
    }

    public static BaseResponse<?> error(int code ,String message){
        return new BaseResponse<>(code,message);
    }
}
