package com.lay.rookie.rookielearning.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Result {
    private Integer code;
    private String msg;
    private Object data;

    Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = new Object();
    }

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 转换器
     *
     * @return
     */
    public String converter() {
        return JSON.toJSONString(this, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 成功
     *
     * @return
     */
    public static String success() {
        return new Result(HttpStatus.OK.value(), HttpStatus.OK.name()).converter();
    }

    /**
     * 成功
     *
     * @param data 数据
     * @return
     */
    public static String success(Object data) {
        return new Result(HttpStatus.OK.value(), HttpStatus.OK.name(), data).converter();
    }

    /**
     * 创建
     * @return
     */
    public static String created() {
        return new Result(HttpStatus.CREATED.value(), HttpStatus.CREATED.name()).converter();
    }

    public static String created(Object data) {
        return new Result(HttpStatus.CREATED.value(), HttpStatus.CREATED.name(), data).converter();
    }

    /**
     * 失败
     *
     * @param msg 错误信息
     * @return
     */
    public static String failure(String msg) {
        return new Result(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg).converter();
    }

    /**
     * 失败
     *
     * @param code 状态码
     * @param msg  错误信息
     * @return
     */
    public static String failure(Integer code, String msg) {
        return new Result(code, msg).converter();
    }
}
