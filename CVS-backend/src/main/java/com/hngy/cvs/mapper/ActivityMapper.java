package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.ActivitySearchDTO;
import com.hngy.cvs.dto.response.ActivityVO;
import com.hngy.cvs.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 活动数据访问层
 *
 * @author CVS Team
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 分页查询活动列表（带搜索条件）
     */
    IPage<ActivityVO> selectActivityPage(IPage<ActivityVO> page, @Param("searchDTO") ActivitySearchDTO searchDTO);

    /**
     * 获取用户创建的活动列表
     */
    IPage<ActivityVO> selectMyActivities(IPage<ActivityVO> page,
                                       @Param("searchDTO") ActivitySearchDTO searchDTO,
                                       @Param("organizerId") Long organizerId);
}
