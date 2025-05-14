package com.swimmingliu.common.response;

import lombok.Data;

@Data
public class Result {

    private Boolean success;

    private Integer code;

    private String message;

    private Object data;

    /*
     * 构造方法私有化，里面的方法都为静态方法
     * 达到保护属性的作用
     */
    private Result() {
    }

    public static Result ok() {
        Result result = new Result();
        result.setSuccess(true);
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        return result;
    }

    public static Result error() {
        Result result = new Result();
        result.setSuccess(false);
        result.setCode(ResultCode.ERROR.getCode());
        result.setMessage(ResultCode.ERROR.getMessage());
        return result;
    }

    public Result message(String message) {
        this.setMessage(message);
        return this;
    }

    public Result code(Integer code) {
        this.setCode(code);
        return this;
    }

    public Result data(Object data) {
        this.setData(data);
        return this;
    }
}