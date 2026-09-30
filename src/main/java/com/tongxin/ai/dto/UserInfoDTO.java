package com.tongxin.ai.dto;

import lombok.Data;

/**
 * 用户信息 DTO（脱敏）
 * 对应接口文档 LoginResponseData.userInfo 结构，剔除 password/status/时间戳等运维字段
 *
 * @author wyq
 */
@Data
public class UserInfoDTO {

    /** 主键ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 角色：0患者/1医生/2管理员 */
    private Integer role;

    /** 头像 */
    private String avatar;
}
