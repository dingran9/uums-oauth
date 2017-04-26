package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.oauth.persist.dao.ManagerLogDao;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerLogPo;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.util.ServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
 * Description:管理员操作日志管理
 */
@Service
public class ManagerLogServiceImpl implements ManagerLogService {

    private final Logger logger = LoggerFactory.getLogger(ManagerLogServiceImpl.class);
    @Resource(name = "entityManagerFactory")
    private EntityManagerFactory emf;
    @Inject
    private ManagerLogDao managerLogDao;
/*    @Inject
    private AuthConverter authConverter;*/

    @Override
    public List<ManagerLogPo> findAll() {
        return (List<ManagerLogPo>) managerLogDao.findAll();
    }

    @Override
    public ManagerLogPo findOne(Long logId) {
        return managerLogDao.findOne(logId);
    }

    @Override
    public ManagerLogPo save(ManagerLogPo ManagerLogPo) {
        return managerLogDao.save(ManagerLogPo);
    }

    @Override
    public ManagerLogPo create(Long managerId, String action,String module, Boolean result,Long productId,String sourceIp,String sourceEquipment,String requestId) {
        ManagerLogPo managerLogPo=new ManagerLogPo();
        managerLogPo.setManagerId(managerId);
        managerLogPo.setAction(action);
        managerLogPo.setResult(result);
        managerLogPo.setModule(module);
        managerLogPo.setProductId(productId);
        managerLogPo.setRequestId(requestId);
        managerLogPo.setSourceEquipment(converterSourceEquipmentType(sourceEquipment));
        return managerLogDao.save(managerLogPo);
    }

    @Override
    public ManagerLogPo create(ManagerPo managerPo, String id, String action, String module, Boolean result,ProductPo productPo, String sourceIp, String sourceEquipment,String requestId) {

        try {
            if(managerPo!=null){
                ManagerLogPo managerLogPo=new ManagerLogPo();
                managerLogPo.setManagerId(managerPo.getId());
                managerLogPo.setAction(action);
                managerLogPo.setResult(result);
                managerLogPo.setModule(module);
                if(productPo!=null){
                    managerLogPo.setProductId(productPo.getId());
                }
                managerLogPo.setRequestId(requestId);
                managerLogPo.setSourceEquipment(converterSourceEquipmentType(sourceEquipment));
                if(id!=null){
                    managerLogPo.setResourceId(id);
                }
                return managerLogDao.save(managerLogPo);
            }
        }catch (Exception e){
            logger.error("create manager logs error:{}",e);
        }

        return null;
    }

    @Override
    public void delete(Long id) {
        managerLogDao.delete(id);
    }

    @Override
    public Page<ManagerLogPo> findPage(final ManagerPo managerPo,final String type, final String keyword,final String param,final String sort, Pageable pageable) {
        return managerLogDao.findAll(new Specification<ManagerLogPo>() {
            public Predicate toPredicate(Root<ManagerLogPo> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                Predicate managerIdP = null;
                if(managerPo==null){
                    return null;
                }
                if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Ordinary)){
                    managerIdP = builder.and(builder.equal(root.get("managerId"), managerPo.getId()));
                }else if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){
                    managerIdP = builder.and(builder.equal(root.get("productId"), managerPo.getProductPo().getId()));
                }

                Predicate predicate = null;
                if ("createDate".equals(type)) {
                    Path<Date> createTime = root.get("createDate");
                    Date beginTime = null;
                    Date endTime = null;
                    if (StringUtils.isNotBlank(keyword) && keyword.contains("|")) {
                        try {
                            String [] keyArr = keyword.split("\\|");
                            if(keyArr.length > 0) {
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
                    if(beginTime != null && endTime != null){
                        predicate = builder.or(builder.between(createTime, beginTime, endTime));
                    }else if(beginTime != null){
                        predicate = builder.or(builder.greaterThanOrEqualTo(createTime, beginTime));
                    }else if(endTime != null){
                        predicate = builder.or(builder.lessThanOrEqualTo(createTime, endTime));
                    }
                }
                if(null == predicate && managerIdP != null){
                    query.where(managerIdP);
                }else if(null != predicate && managerIdP!=null){
                    query.where(managerIdP, predicate);
                }else  if(null != predicate){
                    query.where(predicate);
                }
                //执行排序
                ServiceHelper.sortQuery(root, query, builder, param, sort);
                return null;
            }
        }, pageable);
    }

	@Override
	public List<ManagerLogPo> findAllTodayRegister(Long productId) {
		String sql="SELECT * FROM auth_manager_log t where t.action='create' and t.module='user' and TO_DAYS(t.create_time)=TO_DAYS(NOW())";
		if(productId!=null){
			sql+=" and t.product_id="+productId;
		}
		EntityManager em = emf.createEntityManager();
		Query query=null;
		query=em.createNativeQuery(sql);
		List<ManagerLogPo> returnList= query.getResultList();
		em.close();
		return returnList;
	}

	@Override
	public List<Object> findUserRegisterData(Date startDate, Date endDate) {
		return managerLogDao.findUsersRegisterData(startDate, endDate);
	}

	@Override
	public List<Object> findUserRegisterGroupByHour(Long productId) {
		String sql = "select DATE_FORMAT(create_time,'%Y-%m-%d %H') hours,COUNT(id) total, product_id\n"
				+ "from (SELECT * FROM auth_manager_log  where action='create' and module='user' and TO_DAYS(create_time)=TO_DAYS(NOW())) a  where 1=1";
		if (productId != null) {
			sql += " and product_id=" + productId;
		}
		sql+=" GROUP BY hours";
		EntityManager em = emf.createEntityManager();
		Query query = null;
		query = em.createNativeQuery(sql);
		List<Object> returnList=query.getResultList();
		em.close();
		return returnList;
	}

	@Override
	public List<Object> findUserRegisterGroupByProductId(Long productId) {
		String sqlString="SELECT COUNT(*) total,product_id FROM auth_manager_log t where t.action='create' and t.module='user' and TO_DAYS(t.create_time)=TO_DAYS(NOW())";
		if(productId!=null){
			sqlString+=" and product_id=?1";
		}
		sqlString+="  GROUP BY product_id";
		EntityManager em = emf.createEntityManager();
		Query query = null;
		query = em.createNativeQuery(sqlString);
		if(productId!=null){
			query.setParameter(1, productId);
		}
		List<Object> returnList=query.getResultList();
		em.close();
		return returnList;
	}
    public SourceEnum.EquipmentType converterSourceEquipmentType(String type) {
        if(StringUtils.isBlank(type)){
            return null;
        }
        switch (type) {
            case "Test":
                return SourceEnum.EquipmentType.Test;
            case "Android":
                return SourceEnum.EquipmentType.Android;
            case "Ios":
                return SourceEnum.EquipmentType.Ios;
            default:
                return SourceEnum.EquipmentType.Web;
        }
    }
	
}
