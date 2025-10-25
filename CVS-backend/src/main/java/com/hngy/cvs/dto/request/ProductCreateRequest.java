package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 商品创建请求DTO
 *
 * @author CVS Team
 */
@Data
@Schema(description = "商品创建请求")
public class ProductCreateRequest {

    @NotBlank(message = "商品名称不能为空")
    @Length(max = 200, message = "商品名称不能超过200字符")
    @Schema(description = "商品名称", example = "中性笔套装")
    private String name;

    @NotBlank(message = "商品描述不能为空")
    @Schema(description = "商品描述", example = "10支装黑色中性笔")
    private String description;

    @NotNull(message = "所需积分不能为空")
    @Min(value = 1, message = "所需积分必须大于0")
    @Schema(description = "所需积分", example = "50")
    private Integer pointsRequired;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Schema(description = "商品图片URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @NotNull(message = "商品分类不能为空")
    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "库存预警值", example = "10")
    private Integer stockWarning;
}
