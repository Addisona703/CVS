package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.cvs.dto.response.ProductVO;
import com.hngy.cvs.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品数据访问层
 *
 * @author CVS Team
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 扣减商品库存（使用乐观锁）
     *
     * @param productId 商品ID
     * @return 更新的行数
     */
    int decreaseStock(@Param("productId") Long productId);

    /**
     * 增加商品库存
     *
     * @param productId 商品ID
     * @return 更新的行数
     */
    int increaseStock(@Param("productId") Long productId);

    /**
     * 获取库存预警商品列表
     *
     * @return 库存不足的商品列表
     */
    List<ProductVO> selectLowStockProducts();
}
