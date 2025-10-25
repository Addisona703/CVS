package com.hngy.cvs.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品视图对象
 *
 * @author CVS Team
 */
@Data
@Schema(description = "商品视图对象")
public class ProductVO {

    @Schema(description = "商品ID", example = "1")
    private Long id;

    @Schema(description = "商品名称", example = "中性笔套装")
    private String name;

    @Schema(description = "商品描述", example = "10支装黑色中性笔")
    private String description;

    @Schema(description = "所需积分", example = "50")
    private Integer pointsRequired;

    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Schema(description = "商品图片URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "商品分类名称", example = "文具用品")
    private String categoryName;

    @Schema(description = "商品状态：0-下架，1-上架", example = "1")
    private Integer status;

    @Schema(description = "商品状态描述", example = "上架")
    private String statusText;

    @Schema(description = "库存预警值", example = "10")
    private Integer stockWarning;

    @Schema(description = "当前用户是否可兑换", example = "true")
    private Boolean canRedeem;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
