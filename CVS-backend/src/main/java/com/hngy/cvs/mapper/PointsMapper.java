package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.cvs.entity.Points;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分数据访问层
 * 
 * @author CVS Team
 */
@Mapper
public interface PointsMapper extends BaseMapper<Points> {
    // 继承 BaseMapper 提供的基本 CRUD 方法
    // 所有复杂查询都在 Service 层使用 MyBatis-Plus 方法实现
}