package com.zhy.http.okhttp;

/**
 * Created by Administrator on 2019/11/30.
 */

public class BaseApi {
    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }
}
