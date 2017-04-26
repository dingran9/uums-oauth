package com.eeduspace.uuims.oauth.persist.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum;
import com.eeduspace.uuims.oauth.persist.po.UserUsageCountPo;

/**
 * 
 * @author zhuchaowei
 * 2016年3月21日
 * Description 用户使用量dao
 */
public interface UserUsageCountDao extends CrudRepository<UserUsageCountPo, Serializable>{
    @Query("select u from  UserUsageCountPo u  where u.createDate>=?1 and u.createDate<=?2 and u.countType=?3 ORDER BY u.createDate ASC")
	List<UserUsageCountPo> findAll(Date sTime,Date eTime,UserUsageCountEnum.CountType countType);
    
    @Query("select u from  UserUsageCountPo u  where u.createDate>=?1 and u.createDate<=?2 and u.productId=?3 and u.countType=?4 ORDER BY u.createDate ASC")
	List<UserUsageCountPo> findAllByProductId(Date sTime,Date eTime,Long productId,UserUsageCountEnum.CountType countType);
}
