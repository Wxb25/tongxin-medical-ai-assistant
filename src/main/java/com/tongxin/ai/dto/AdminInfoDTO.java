package com.tongxin.ai.dto;

import lombok.Data;

/**
 * 管理员信息 DTO（脱敏，不含密码）
 *
 * @author wyq
 */
@Data
public class AdminInfoDTO {

    private Long id;
    private String username;
    private String realName;
}
