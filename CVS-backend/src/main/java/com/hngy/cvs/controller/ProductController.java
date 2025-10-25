package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.ProductCreateRequest;
import com.hngy.cvs.dto.request.ProductQueryRequest;
import com.hngy.cvs.dto.request.ProductUpdateRequest;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.ProductVO;
import com.hngy.cvs.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 *
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "商品管理", description = "商品相关接口")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建商品")
    public Result<Long> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Long productId = productService.createProduct(request);
        return Result.success("商品创建成功", productId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新商品")
    public Result<Void> updateProduct(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        productService.updateProduct(id, request);
        return Result.success("商品更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除商品")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }

    @PostMapping("/list")
    @Operation(summary = "获取商品列表（分页、筛选）")
    public Result<PageVO<ProductVO>> getProductList(
            @Valid @RequestBody ProductQueryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        // 如果用户已登录，传递用户ID用于判断是否可兑换
        Long userId = principal != null ? principal.getUserId() : null;
        
        PageVO<ProductVO> products = productService.getProductList(request, userId);
        return Result.success(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<ProductVO> getProductDetail(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        // 如果用户已登录，传递用户ID用于判断是否可兑换
        Long userId = principal != null ? principal.getUserId() : null;
        ProductVO product = productService.getProductDetail(id, userId);
        return Result.success(product);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新商品状态")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long id,
            @Parameter(description = "商品状态：0-下架，1-上架") @RequestParam @NotNull Integer status) {
        productService.updateProductStatus(id, status);
        return Result.success("商品状态更新成功");
    }

    @GetMapping("/{id}/stock/check")
    @Operation(summary = "检查商品库存")
    public Result<Boolean> checkStock(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long id) {
        boolean hasStock = productService.checkStock(id);
        return Result.success(hasStock);
    }
}