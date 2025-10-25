package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.dto.request.CategoryCreateRequest;
import com.hngy.cvs.dto.request.CategoryUpdateRequest;
import com.hngy.cvs.dto.response.CategoryVO;
import com.hngy.cvs.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理控制器
 *
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "商品分类管理", description = "商品分类相关接口")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建分类")
    public Result<Long> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Long categoryId = categoryService.createCategory(request);
        return Result.success("分类创建成功", categoryId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        categoryService.updateCategory(id, request);
        return Result.success("分类更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功");
    }

    @GetMapping
    @Operation(summary = "获取所有分类")
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    public Result<CategoryVO> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id) {
        CategoryVO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
}