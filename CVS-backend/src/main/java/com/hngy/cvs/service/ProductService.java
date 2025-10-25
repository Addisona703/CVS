package com.hngy.cvs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.cvs.dto.request.ProductCreateRequest;
import com.hngy.cvs.dto.request.ProductQueryRequest;
import com.hngy.cvs.dto.request.ProductUpdateRequest;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.ProductVO;
import com.hngy.cvs.entity.Product;

/**
 * 商品服务接口
 *
 * @author CVS Team
 */
public interface ProductService extends IService<Product> {

    /**
     * 创建商品
     *
     * @param request 商品创建请求
     * @return 商品ID
     */
    Long createProduct(ProductCreateRequest request);

    /**
     * 更新商品
     *
     * @param id 商品ID
     * @param request 商品更新请求
     */
    void updateProduct(Long id, ProductUpdateRequest request);

    /**
     * 删除商品（逻辑删除）
     *
     * @param id 商品ID
     */
    void deleteProduct(Long id);

    /**
     * 获取商品列表（分页、筛选）
     *
     * @param request 查询请求
     * @param userId 用户ID（可选，用于判断是否可兑换）
     * @return 分页商品列表
     */
    PageVO<ProductVO> getProductList(ProductQueryRequest request, Long userId);

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @param userId 用户ID（可选，用于判断是否可兑换）
     * @return 商品详情
     */
    ProductVO getProductDetail(Long id, Long userId);

    /**
     * 更新商品状态
     *
     * @param id 商品ID
     * @param status 商品状态：0-下架，1-上架
     */
    void updateProductStatus(Long id, Integer status);

    /**
     * 检查库存
     *
     * @param productId 商品ID
     * @return 是否有库存
     */
    boolean checkStock(Long productId);

    /**
     * 扣减库存（使用乐观锁）
     *
     * @param productId 商品ID
     * @return 是否扣减成功
     */
    boolean decreaseStock(Long productId);

    /**
     * 增加库存
     *
     * @param productId 商品ID
     */
    void increaseStock(Long productId);
}
