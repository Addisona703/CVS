package com.hngy.cvs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.SignInOutDTO;
import com.hngy.cvs.dto.request.SignupApprovalDTO;
import com.hngy.cvs.dto.request.SignupCreateDTO;
import com.hngy.cvs.dto.request.SignupSearchDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.SignupVO;
import com.hngy.cvs.dto.response.PageVO;

/**
 * 报名服务接口
 * 
 * @author CVS Team
 */
public interface SignupService {

    /**
     * 报名活动
     */
    SignupVO signupActivity(SignupCreateDTO request, Long userId);

    /**
     * 根据ID获取报名详情
     */
    SignupVO getSignupById(Long id);

    /**
     * 分页查询报名列表
     * 支持按活动名称、学生名称模糊查询，按报名状态筛选
     */
    PageVO<SignupVO> getSignupList(PageDTO<SignupSearchDTO> pageRequest);
}
