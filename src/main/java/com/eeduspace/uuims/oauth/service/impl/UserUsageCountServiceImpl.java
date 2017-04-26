package com.eeduspace.uuims.oauth.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.springframework.stereotype.Service;

import com.eeduspace.uuims.oauth.persist.dao.UserUsageCountDao;
import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserUsageCountEnum.CountType;
import com.eeduspace.uuims.oauth.persist.po.UserLogPo;
import com.eeduspace.uuims.oauth.persist.po.UserUsageCountPo;
import com.eeduspace.uuims.oauth.service.UserUsageCountService;
@Service
public class UserUsageCountServiceImpl implements UserUsageCountService {
	@Inject
	UserUsageCountDao usageCountDao;
	@Resource(name = "entityManagerFactory")
    private EntityManagerFactory emf;
	@Override
	public UserUsageCountPo save(UserUsageCountPo usageCountPo) {
		return usageCountDao.save(usageCountPo);
	}

	@Override
	public List<UserUsageCountPo> findAll(Date startDate, Date endDate,UserUsageCountEnum.CountType countType) {
		return usageCountDao.findAll(startDate, endDate,countType);
	}

	@Override
	public List<UserUsageCountPo> findAllByProductId(Date startDate,
			Date endDate, Long productId,UserUsageCountEnum.CountType countType) {
		return usageCountDao.findAllByProductId(startDate, endDate, productId,countType);
	}

	@Override
	public List<Object> findGroupData(Date startDate, Date endDate,
			Long productId, CountType countType, String groupType) {
		if(groupType==null){
			return new ArrayList<>();
		}
		if(groupType.equals("group_by_day")){
			String sql="select DATE_FORMAT(create_time,'%Y-%m-%d') days,SUM(count_total) total,product_id,product_name from auth_user_usage_count where 1=1";
			EntityManager em = emf.createEntityManager();
			if(productId!=null){
				sql+=" and product_id="+productId;
			}
			if(countType!=null){
				sql+=" and count_type="+countType.getValue();
			}
			sql+=" and create_time>=?1";
			sql+=" and create_time<=?2";
			if(productId!=null){
				sql+=" group by days,product_id";
			}else{
				sql+=" group by days";
			}
			Query query=em.createNativeQuery(sql);
			query.setParameter(1, startDate);
			query.setParameter(2, endDate);
			List<Object> returnList=query.getResultList();
			em.close();
			return returnList;
		}else if(groupType.equals("group_by_month")){
			String sql="select DATE_FORMAT(create_time,'%Y-%m') months,SUM(count_total) total,product_id,product_name  from auth_user_usage_count  where 1=1";
			EntityManager em = emf.createEntityManager();
			if(productId!=null){
				sql+=" and product_id="+productId;
			}
			if(countType!=null){
				sql+=" and count_type="+countType.getValue();
			}
			sql+=" and create_time>=?1";
			sql+=" and create_time<=?2";
			if(productId!=null){
				sql+=" group by months,product_id";
			}else{
				sql+=" group by months";
			}
			Query query=em.createNativeQuery(sql);
			query.setParameter(1, startDate);
			query.setParameter(2, endDate);
			List<Object> returnList=query.getResultList();
			em.close();
			return returnList;
		}else{
			return new ArrayList<>();
		}
	}

	@Override
	public List<Object> findGroupByProductId(Date startDate, Date endDate,
			CountType countType,Long productId) {
		String sqlString="SELECT SUM(count_total) total,product_name,product_id FROM auth_user_usage_count where 1=1 and create_time>=?1 and create_time<=?2  and count_type=?3 ";
		if(productId!=null){
			sqlString+=" and product_id=?4";
		}
		sqlString+=" GROUP BY product_id";
		EntityManager em = emf.createEntityManager();
		Query query=em.createNativeQuery(sqlString);
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
		query.setParameter(3, countType.getValue());
		if(productId!=null){
			query.setParameter(4, productId);
		}
		List<Object> returnList=query.getResultList();
		em.close();
		return returnList;
	}

}
