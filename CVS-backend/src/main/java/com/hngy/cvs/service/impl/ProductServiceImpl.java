package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.ProductCreateRequest;
import com.hngy.cvs.dto.request.ProductQueryRequest;
import com.hngy.cvs.dto.request.ProductUpdateRequest;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.ProductVO;
import com.hngy.cvs.entity.Category;
import com.hngy.cvs.entity.Points;
import com.hngy.cvs.entity.Product;
import com.hngy.cvs.entity.enums.ProductStatus;
import com.hngy.cvs.mapper.CategoryMapper;
import com.hngy.cvs.mapper.PointsMapper;
import com.hngy.cvs.mapper.ProductMapper;
import com.hngy.cvs.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryMapper categoryMapper;
    private final PointsMapper pointsMapper;

    @Override
    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        // 参数校验
        AssertUtils.notNull(request, "商品创建请求不能为空");
        
        // 验证分类存在
        Category category = categoryMapper.selectById(request.getCategoryId());
        AssertUtils.notNull(category, "商品分类不存在");
        
        // 创建商品实体
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setStatus(ProductStatus.OFFLINE.getCode()); // 默认下架状态
        
        // 设置默认库存预警值
        if (product.getStockWarning() == null) {
            product.setStockWarning(10);
        }
        
        boolean success = this.save(product);
        AssertUtils.isTrue(success, "商品创建失败");
        
        log.info("成功创建商品: {}, ID: {}", product.getName(), product.getId());
        return product.getId();
    }

    @Override
    @Transactional
    public void updateProduct(Long id, ProductUpdateRequest request) {
        // 参数校验
        AssertUtils.notNull(id, "商品ID不能为空");
        AssertUtils.notNull(request, "商品更新请求不能为空");
        
        // 验证商品存在
        Product product = this.getById(id);
        AssertUtils.notNull(product, "商品不存在");
        
        // 验证分类存在（如果要更新分类）
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            AssertUtils.notNull(category, "商品分类不存在");
        }
        
        // 更新商品信息
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            product.setDescription(request.getDescription());
        }
        if (request.getPointsRequired() != null) {
            product.setPointsRequired(request.getPointsRequired());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getStatus() != null) {
            AssertUtils.notNull(ProductStatus.fromCode(request.getStatus()), "商品状态无效");
            product.setStatus(request.getStatus());
        }
        if (request.getStockWarning() != null) {
            product.setStockWarning(request.getStockWarning());
        }
        
        boolean success = this.updateById(product);
        AssertUtils.isTrue(success, "商品更新失败");
        
        log.info("成功更新商品: {}, ID: {}", product.getName(), product.getId());
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        // 参数校验
        AssertUtils.notNull(id, "商品ID不能为空");
        
        // 验证商品存在
        Product product = this.getById(id);
        AssertUtils.notNull(product, "商品不存在");
        
        // 逻辑删除
        boolean success = this.removeById(id);
        AssertUtils.isTrue(success, "商品删除失败");
        
        log.info("成功删除商品: {}, ID: {}", product.getName(), product.getId());
    }

    @Override
    public PageVO<ProductVO> getProductList(ProductQueryRequest request, Long userId) {
        // 参数校验
        AssertUtils.notNull(request, "查询请求不能为空");
        AssertUtils.isTrue(request.getPageNum() > 0, "页码必须大于0");
        AssertUtils.isTrue(request.getPageSize() > 0, "每页大小必须大于0");
        AssertUtils.isTrue(request.getPageSize() <= 100, "每页大小不能超过100");
        
        log.debug("获取商品列表，页码: {}, 每页大小: {}, 查询条件: {}", 
                request.getPageNum(), request.getPageSize(), request);
        
        // 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getKeyword()), Product::getName, request.getKeyword())
               .eq(request.getCategoryId() != null, Product::getCategoryId, request.getCategoryId())
               .ge(request.getMinPoints() != null, Product::getPointsRequired, request.getMinPoints())
               .le(request.getMaxPoints() != null, Product::getPointsRequired, request.getMaxPoints());
        
        // 状态过滤逻辑：
        // 1. 如果请求中指定了状态，使用指定的状态（管理员端筛选）
        // 2. 如果没有指定状态，默认只显示上架商品（学生端）
        if (request.getStatus() != null) {
            wrapper.eq(Product::getStatus, request.getStatus());
        } else {
            // 默认只显示上架商品
            wrapper.eq(Product::getStatus, ProductStatus.ONLINE.getCode());
        }
        
        wrapper.orderByDesc(Product::getCreatedAt);
        
        // 分页查询
        Page<Product> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Product> productPage = this.page(page, wrapper);
        
        // 转换为VO
        List<Product> products = productPage.getRecords();
        List<ProductVO> productVOs = convertToVOList(products, userId);
        
        // 构造分页结果
        Page<ProductVO> resultPage = new Page<>(request.getPageNum(), request.getPageSize(), productPage.getTotal());
        resultPage.setRecords(productVOs);
        
        return PageUtil.convert(resultPage);
    }

    @Override
    public ProductVO getProductDetail(Long id, Long userId) {
        // 参数校验
        AssertUtils.notNull(id, "商品ID不能为空");
        
        // 获取商品信息
        Product product = this.getById(id);
        AssertUtils.notNull(product, "商品不存在");
        
        // 转换为VO
        ProductVO productVO = convertToVO(product, userId);
        
        log.debug("获取商品详情: {}, ID: {}", product.getName(), product.getId());
        return productVO;
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, Integer status) {
        // 参数校验
        AssertUtils.notNull(id, "商品ID不能为空");
        AssertUtils.notNull(status, "商品状态不能为空");
        AssertUtils.notNull(ProductStatus.fromCode(status), "商品状态无效");
        
        // 验证商品存在
        Product product = this.getById(id);
        AssertUtils.notNull(product, "商品不存在");
        
        // 更新状态
        product.setStatus(status);
        boolean success = this.updateById(product);
        AssertUtils.isTrue(success, "商品状态更新失败");
        
        log.info("成功更新商品状态: {}, ID: {}, 状态: {}", 
                product.getName(), product.getId(), ProductStatus.fromCode(status).getDescription());
    }

    @Override
    public boolean checkStock(Long productId) {
        // 参数校验
        AssertUtils.notNull(productId, "商品ID不能为空");
        
        Product product = this.getById(productId);
        AssertUtils.notNull(product, "商品不存在");
        
        return product.getStock() != null && product.getStock() > 0;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Long productId) {
        // 参数校验
        AssertUtils.notNull(productId, "商品ID不能为空");
        
        // 使用乐观锁扣减库存
        int updatedRows = baseMapper.decreaseStock(productId);
        boolean success = updatedRows > 0;
        
        if (success) {
            log.info("成功扣减商品库存，商品ID: {}", productId);
        } else {
            log.warn("扣减商品库存失败，可能库存不足或商品不存在，商品ID: {}", productId);
        }
        
        return success;
    }

    @Override
    @Transactional
    public void increaseStock(Long productId) {
        // 参数校验
        AssertUtils.notNull(productId, "商品ID不能为空");
        
        // 增加库存
        int updatedRows = baseMapper.increaseStock(productId);
        AssertUtils.isTrue(updatedRows > 0, "增加商品库存失败");
        
        log.info("成功增加商品库存，商品ID: {}", productId);
    }

    /**
     * 转换商品列表为VO列表
     */
    private List<ProductVO> convertToVOList(List<Product> products, Long userId) {
        if (products.isEmpty()) {
            return List.of();
        }
        
        // 获取分类信息
        Set<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().in(Category::getId, categoryIds)
        ).stream().collect(Collectors.toMap(Category::getId, category -> category));
        
        // 获取用户积分（如果提供了用户ID）
        Integer userPoints = null;
        if (userId != null) {
            Points points = pointsMapper.selectOne(
                    new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
            );
            userPoints = points != null ? points.getPoints() : 0;
        }
        
        // 转换为VO
        final Integer finalUserPoints = userPoints;
        return products.stream()
                .map(product -> convertToVO(product, categoryMap.get(product.getCategoryId()), finalUserPoints))
                .collect(Collectors.toList());
    }

    /**
     * 转换单个商品为VO
     */
    private ProductVO convertToVO(Product product, Long userId) {
        // 获取分类信息
        Category category = categoryMapper.selectById(product.getCategoryId());
        
        // 获取用户积分
        Integer userPoints = null;
        if (userId != null) {
            Points points = pointsMapper.selectOne(
                    new LambdaQueryWrapper<Points>().eq(Points::getUserId, userId)
            );
            userPoints = points != null ? points.getPoints() : 0;
        }
        
        return convertToVO(product, category, userPoints);
    }

    /**
     * 转换商品为VO（内部方法）
     */
    private ProductVO convertToVO(Product product, Category category, Integer userPoints) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        
        // 设置分类信息
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        // 设置状态描述
        ProductStatus status = ProductStatus.fromCode(product.getStatus());
        if (status != null) {
            vo.setStatusText(status.getDescription());
        }
        
        // 判断是否可兑换
        boolean canRedeem = false;
        if (userPoints != null && product.getStatus() != null && product.getStock() != null) {
            canRedeem = ProductStatus.ONLINE.getCode().equals(product.getStatus()) 
                    && product.getStock() > 0 
                    && userPoints >= product.getPointsRequired();
        }
        vo.setCanRedeem(canRedeem);
        
        return vo;
    }
}