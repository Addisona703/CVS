package com.hngy.cvs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.cvs.dto.request.CategoryCreateRequest;
import com.hngy.cvs.dto.request.CategoryUpdateRequest;
import com.hngy.cvs.dto.response.CategoryVO;
import com.hngy.cvs.entity.Category;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author CVS Team
 */
public interface CategoryService extends IService<Category> {

    /**
     * 创建分类
     *
     * @param request 创建分类请求
     * @return 分类ID
     */
    Long createCategory(CategoryCreateRequest request);

    /**
     * 更新分类
     *
     * @param id 分类ID
     * @param request 更新分类请求
     */
    void updateCategory(Long id, CategoryUpdateRequest request);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<CategoryVO> getAllCategories();

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryVO getCategoryById(Long id);
}