package com.swimmingliu.common.response;

public interface CustomizeResultCode {
    /*
   获取错误码
   @return 错误状态码
    */
    Integer getCode();

    /*
    获取错误信息
    @return 错误信息
     */
    String getMessage();
}