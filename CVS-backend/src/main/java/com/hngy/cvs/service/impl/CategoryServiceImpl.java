package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.dto.request.CategoryCreateRequest;
import com.hngy.cvs.dto.request.CategoryUpdateRequest;
import com.hngy.cvs.dto.response.CategoryVO;
import com.hngy.cvs.entity.Category;
import com.hngy.cvs.entity.Product;
import com.hngy.cvs.mapper.CategoryMapper;
import com.hngy.cvs.mapper.ProductMapper;
import com.hngy.cvs.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 *
 * @author CVS Team
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CategoryCreateRequest request) {
        // 检查分类名称是否已存在
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, request.getName());
        Category existingCategory = categoryMapper.selectOne(queryWrapper);
        if (existingCategory != null) {
            throw new BusinessException(ResultCode.CONFLICT, "分类名称已存在");
        }

        // 创建分类
        Category category = new Category();
        BeanUtils.copyProperties(request, category);
        
        // 如果没有设置排序顺序，设置为0
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }

        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, CategoryUpdateRequest request) {
        // 检查分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 检查分类名称是否已被其他分类使用
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, request.getName())
                   .ne(Category::getId, id);
        Category existingCategory = categoryMapper.selectOne(queryWrapper);
        if (existingCategory != null) {
            throw new BusinessException(ResultCode.CONFLICT, "分类名称已存在");
        }

        // 更新分类信息
        BeanUtils.copyProperties(request, category);
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 检查分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 检查分类下是否有商品
        LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.eq(Product::getCategoryId, id);
        Long productCount = productMapper.selectCount(productQueryWrapper);
        if (productCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_PRODUCTS);
        }

        // 删除分类（逻辑删除）
        categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        // 查询所有分类，按排序顺序排列
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSortOrder, Category::getId);
        List<Category> categories = categoryMapper.selectList(queryWrapper);

        // 转换为VO
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        return convertToVO(category);
    }

    /**
     * 将Category实体转换为CategoryVO
     *
     * @param category 分类实体
     * @return 分类VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}