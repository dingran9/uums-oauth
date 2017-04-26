package com.eeduspace.uuims.oauth.persist.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eeduspace.uuims.oauth.persist.po.ManagerLogPo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface ManagerLogDao extends CrudRepository<ManagerLogPo, Long> {
    /**
     * 分页查询
     * @param spec
     * @param pageable
     * @return
     */
    @Query("select m from ManagerLogPo m")
    Page<ManagerLogPo> findAll(Specification<ManagerLogPo> spec, Pageable pageable);
    
    /**
     * 查询用户注册记录数据
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月24日 上午9:06:59
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("select count(m.id) as totalCount ,m.productId as productId from ManagerLogPo m where m.action='create' and m.module='user' and m.createDate>=?1 and m.createDate<=?2 GROUP BY m.productId ")
    List<Object> findUsersRegisterData(Date startDate,Date endDate);
}
