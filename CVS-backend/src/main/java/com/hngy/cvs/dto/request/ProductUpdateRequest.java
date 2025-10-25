package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 商品更新请求DTO
 *
 * @author CVS Team
 */
@Data
@Schema(description = "商品更新请求")
public class ProductUpdateRequest {

    @Schema(description = "商品ID", example = "1")
    private Long id;

    @Length(max = 200, message = "商品名称不能超过200字符")
    @Schema(description = "商品名称", example = "中性笔套装")
    private String name;

    @Schema(description = "商品描述", example = "10支装黑色中性笔")
    private String description;

    @Min(value = 1, message = "所需积分必须大于0")
    @Schema(description = "所需积分", example = "50")
    private Integer pointsRequired;

    @Min(value = 0, message = "库存数量不能为负数")
    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Schema(description = "商品图片URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "商品状态：0-下架，1-上架", example = "1")
    private Integer status;

    @Schema(description = "库存预警值", example = "10")
    private Integer stockWarning;
}
