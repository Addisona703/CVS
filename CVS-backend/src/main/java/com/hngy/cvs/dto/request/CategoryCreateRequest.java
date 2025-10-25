package com.hngy.cvs.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建商品分类请求DTO
 *
 * @author CVS Team
 */
@Data
public class CategoryCreateRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不能超过100字符")
    private String name;

    /**
     * 分类描述
     */
    @Size(max = 500, message = "分类描述不能超过500字符")
    private String description;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}