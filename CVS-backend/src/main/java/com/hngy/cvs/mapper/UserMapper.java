package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.cvs.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户数据访问层
 * 
 * @author CVS Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {



    /**
     * 统计各角色用户数量
     */
    @Select("SELECT role, COUNT(*) as count FROM user_twb WHERE deleted = 0 GROUP BY role")
    List<Map<String, Object>> countByRole();


}
