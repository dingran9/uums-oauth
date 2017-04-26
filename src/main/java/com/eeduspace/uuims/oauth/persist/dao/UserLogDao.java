package com.eeduspace.uuims.oauth.persist.dao;

import java.util.Date;
import java.util.List;

import com.eeduspace.uuims.oauth.persist.po.UserLogPo;

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
public interface UserLogDao extends CrudRepository<UserLogPo, Long> {

    /**
     * 分页查询
     * @param spec
     * @param pageable
     * @return
     */
    @Query("select m from UserLogPo m")
    Page<UserLogPo> findAll(Specification<UserLogPo> spec, Pageable pageable);
    /**
     * 获取 action 为login的日志数据
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月23日 下午5:48:47
     * @param startDate
     * @param endDate
     * @return
     */
   // @Query("select u from UserLogPo u where u.action='login' and u.createDate>=?1 and u.createDate<=?2")
    @Query("select  COUNT(DISTINCT user_id), u.productId from UserLogPo u where u.action='login' and u.createDate>=?1 and u.createDate<=?2  GROUP BY u.productId")
    List<Object> findActiveUsersData(Date startDate,Date endDate);
}
