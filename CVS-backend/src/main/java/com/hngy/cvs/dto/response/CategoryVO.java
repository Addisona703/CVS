package com.hngy.cvs.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类视图对象
 *
 * @author CVS Team
 */
@Data
public class CategoryVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}