package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品查询请求DTO
 *
 * @author CVS Team
 */
@Data
@Schema(description = "商品查询请求")
public class ProductQueryRequest extends PageDTO<ProductQueryRequest> {

    @Schema(description = "商品名称关键词", example = "笔")
    private String keyword;

    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "商品状态：0-下架，1-上架", example = "1")
    private Integer status;

    @Schema(description = "最小积分", example = "0")
    private Integer minPoints;

    @Schema(description = "最大积分", example = "1000")
    private Integer maxPoints;
}
