package com.tongxin.ai.dto;

import lombok.Data;

/**
 * 排班条目（对应 doctors.schedule JSONB 数组元素）
 * 库里格式: {"period": "AM", "weekday": 1, "maxCount": 20}
 *
 * @author wyq
 */
@Data
public class ScheduleItem {

    /** 时段：AM（上午）/ PM（下午） */
    private String period;

    /** 星期几：1=周一, 2=周二, ... 7=周日 */
    private Integer weekday;

    /** 该时段号源总数 */
    private Integer maxCount;
}
