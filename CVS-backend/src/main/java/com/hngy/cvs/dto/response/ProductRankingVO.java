package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品兑换排行视图对象
 *
 * @author CVS Team
 */
@Data
@Schema(description = "商品兑换排行视图对象")
public class ProductRankingVO {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "中性笔套装")
    private String productName;

    @Schema(description = "商品图片URL", example = "https://example.com/image.jpg")
    private String productImageUrl;

    @Schema(description = "商品分类名称", example = "文具用品")
    private String categoryName;

    @Schema(description = "兑换次数", example = "45")
    private Long redemptionCount;

    @Schema(description = "总消耗积分", example = "2250")
    private Long totalPointsSpent;

    @Schema(description = "当前库存", example = "55")
    private Integer currentStock;

    @Schema(description = "排名", example = "1")
    private Integer rank;
}