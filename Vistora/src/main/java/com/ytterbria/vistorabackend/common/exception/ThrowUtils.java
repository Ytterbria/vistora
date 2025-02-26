package com.ytterbria.vistorabackend.common.exception;

public class ThrowUtils {
    public static void throwIf(boolean condition,RuntimeException runtimeException){
        if(condition) {
            throw runtimeException;
        }
    }

    /**
     * java方法的重载机制，可以根据参数的类型来选择调用哪个方法。
     */
    public static void throwIf(boolean condition,ErrorCode errorCode){
        if(condition){
            throw new BusinessException(errorCode);
        }
    }

    public static void throwIf(boolean condition,ErrorCode errorCode,String message){
        if(condition){
            throw new BusinessException(errorCode,message);
        }
    }

}
