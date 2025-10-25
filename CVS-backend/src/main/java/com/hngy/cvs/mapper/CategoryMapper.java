package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.cvs.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类数据访问层
 *
 * @author CVS Team
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // 继承 BaseMapper 提供的基本 CRUD 方法
}
