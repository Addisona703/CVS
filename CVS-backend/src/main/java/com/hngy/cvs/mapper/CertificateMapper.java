package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.CertificateVO;
import com.hngy.cvs.entity.Certificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 证明数据访问层
 * 
 * @author CVS Team
 */
@Mapper
public interface CertificateMapper extends BaseMapper<Certificate> {

    /**
     * 查询用户的证明记录
     */
    IPage<CertificateVO> selectMyCertificates(IPage<CertificateVO> page, @Param("userId") Long userId);

    /**
     * 查询所有证明记录（管理员）
     */
    IPage<CertificateVO> selectAllCertificates(IPage<CertificateVO> page);

}
