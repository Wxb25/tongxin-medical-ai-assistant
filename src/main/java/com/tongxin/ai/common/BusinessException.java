package com.tongxin.ai.common;

import lombok.Getter;

/**
 * 业务异常
 * 用于在 Service 层抛出可预期的业务错误（如用户名已存在、密码错误等）
 *
 * @author wyq
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
