package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.persist.dao.UserLogDao;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserLogPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.service.EventOperationService;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.oauth.util.ServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:用户操作日志管理
 */
@Service
public class UserLogServiceImpl implements UserLogService {

    private final Logger logger = LoggerFactory.getLogger(UserLogServiceImpl.class);
    @Resource(name = "entityManagerFactory")
    private EntityManagerFactory emf;
    @Inject
    private UserLogDao userLogDao;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private UserService userService;
    @Inject
    private EventOperationService eventOperationService;
    @Override
    public List<UserLogPo> findAll() {
        return (List<UserLogPo>) userLogDao.findAll();
    }

    @Override
    public UserLogPo findOne(Long logId) {
        return userLogDao.findOne(logId);
    }

    @Override
    public UserLogPo save(UserLogPo UserLogPo) {
        return userLogDao.save(UserLogPo);
    }

    @Override
    @Async
    public void create(UserPo userPo,String action, String module, Boolean result, Long productId, String sourceIp, String sourceEquipment,String requestId) {
     try {
         if(userPo!=null){
             UserLogPo userLogPo=new UserLogPo();
             userLogPo.setUserPo(userPo);
             userLogPo.setAction(action);
             userLogPo.setResult(result);
             userLogPo.setModule(module);
             userLogPo.setProductId(userPo.getRegisterProductId());
             userLogPo.setRequestId(requestId);
             userLogPo.setSourceEquipment(authConverter.converterSourceEquipmentType(sourceEquipment));
             eventOperationService.createUserLogMessage(userLogPo);
         }
     }catch (Exception e){
         logger.error("create logs error:{}",e);
     }
    }

    @Override
    public UserLogPo create(Long userId, String action, String module, Boolean result, Long productId, String sourceIp, String sourceEquipment,String requestId) {
        UserLogPo userLogPo=new UserLogPo();
        userLogPo.setUserPo(userService.findOne(userId));
        userLogPo.setAction(action);
        userLogPo.setResult(result);
        userLogPo.setModule(module);
        userLogPo.setProductId(productId);
        userLogPo.setRequestId(requestId);
        userLogPo.setSourceEquipment(authConverter.converterSourceEquipmentType(sourceEquipment));
        return userLogDao.save(userLogPo);
    }

    @Override
    public void delete(Long id) {
        userLogDao.delete(id);
    }

    @Override
    public Page<UserLogPo> findPage( final String accountPhone,final ManagerPo managerPo,final String type,final  String keyword,final  String param,final  String sort, Pageable pageable) {

        logger.debug("----------type"+type);
        logger.debug("----------keyword"+keyword);
        logger.debug("----------sort"+sort);
        logger.debug("----------productId"+managerPo.getProductPo());
        logger.debug("----------accountId"+accountPhone);
        return userLogDao.findAll(new Specification<UserLogPo>() {
            public Predicate toPredicate(Root<UserLogPo> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                Predicate userPredicate = null;
                Predicate predicate = null;
                if(managerPo.getProductPo()!=null){
                    userPredicate = builder.and(builder.equal(root.get("productId"), managerPo.getProductPo().getId()));
                }
                if("".equals(type)){
                    Path<Date> createTime = root.get("createDate");
                    Date beginTime = null;
                    Date endTime = null;
                    if (StringUtils.isNotBlank(keyword) && keyword.contains("|")) {
                        try {
                            String[] keyArr = keyword.split("\\|");
                            System.out.println("keyArr.length:" + keyArr.length);
                            if (keyArr.length > 0) {
                                if (keyword.startsWith("|")) {
                                    endTime = DateUtils.parseDate(keyword.substring(1, keyword.length()), DateUtils.DATE_FORMAT_DATETIME);
                                } else if (keyword.endsWith("|")) {
                                    beginTime = DateUtils.parseDate(keyword.substring(0, keyword.length() - 1), DateUtils.DATE_FORMAT_DATEONLY);
                                } else if (keyArr.length == 2) {
                                    if (StringUtils.isNotBlank(keyArr[0]))
                                        beginTime = DateUtils.parseDate(keyArr[0], DateUtils.DATE_FORMAT_DATEONLY);
                                    if (StringUtils.isNotBlank(keyArr[1]))
                                        endTime = DateUtils.parseDate(keyArr[1], DateUtils.DATE_FORMAT_DATETIME);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (beginTime != null && endTime != null) {
                        predicate = builder.or(builder.between(createTime, beginTime, endTime));
                    } else if (beginTime != null) {
                        predicate = builder.or(builder.greaterThanOrEqualTo(createTime, beginTime));
                    } else if (endTime != null) {
                        predicate = builder.or(builder.lessThanOrEqualTo(createTime, endTime));
                    }
                }else if("account".equals(type)){
                    if (StringUtils.isNotBlank(keyword)) {
                        predicate = builder.and(builder.like((Path) root.get("userPo").get("phone"), "%" + keyword + "%"));
                    }
                }
                if(null==userPredicate){
                    if (null != predicate) {
                        query.where(predicate);
                    }
                }else {
                    if (null == predicate) {
                        query.where(userPredicate);
                    } else {
                        query.where(userPredicate, predicate);
                    }
                }
                //执行排序
                ServiceHelper.sortQuery(root, query, builder, param, sort);
                return null;
            }
        }, pageable);
    }

	@Override
	public List<UserLogPo> findTodayAllData(Long productId) {
		String sql="SELECT * FROM auth_user_log t  where TO_DAYS(t.create_time)=TO_DAYS(NOW()) and t.action='login' ";
		if(productId!=null){
			sql+=" and t.product_id="+productId;
		}
		sql+=" GROUP BY user_id";
		EntityManager em = emf.createEntityManager();
		Query query=null;
		query=em.createNativeQuery(sql,UserLogPo.class);
		List<UserLogPo> returnList=query.getResultList();
		em.close();
		return returnList;
	}

	@Override
	public List<Object> findUserActiveData(Date startDate, Date endDate) {
		return userLogDao.findActiveUsersData(startDate, endDate);
	}

	@Override
	public List<Object> findUserActiveGroupByHour(Long productId) {
		String sql="select DATE_FORMAT(create_time,'%Y-%m-%d %H') hours,COUNT(id) total, product_id\n" +
				"from (SELECT * FROM auth_user_log where action='login' and TO_DAYS(create_time)=TO_DAYS(NOW()) GROUP BY user_id) a where 1=1 ";
		if(productId!=null){
			sql+=" and product_id="+productId;
		}
		sql+=" GROUP BY hours";
		EntityManager em = emf.createEntityManager();
		Query  query=em.createNativeQuery(sql);
		List<Object> returnList=query.getResultList();
		em.close();
		return returnList;
	}

	@Override
	public List<Object> findUserActeiceGroupByProductId(Long productId) {
		String sqlString="SELECT COUNT(*) total,a.product_id  from (SELECT * FROM auth_user_log t  where TO_DAYS(t.create_time)=TO_DAYS(NOW()) and t.action='login' GROUP BY user_id) a ";
		if(productId!=null){
			sqlString+="  where 1=1 and a.product_id=?1";
		}
		sqlString+=" GROUP BY a.product_id";
		EntityManager em = emf.createEntityManager();
		Query  query=em.createNativeQuery(sqlString);
		if(productId!=null){
			query.setParameter(1, productId);
		}
		List<Object> returnList=query.getResultList();
		em.close();
		return returnList;
	}

}
