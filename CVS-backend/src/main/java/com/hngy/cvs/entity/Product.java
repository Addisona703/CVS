package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author CVS Team
 */
@Data
@TableName("product_twb")
public class Product {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 所需积分
     */
    private Integer pointsRequired;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品图片URL
     */
    private String imageUrl;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 库存预警值
     */
    private Integer stockWarning;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
