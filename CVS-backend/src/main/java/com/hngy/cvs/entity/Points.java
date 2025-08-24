package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 积分实体类
 * 
 * @author CVS Team
 */
@Data
@TableName("points_twb")
public class Points {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 积分数量
     */
    private Integer points;
}
