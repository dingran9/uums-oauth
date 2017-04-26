package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.oauth.persist.dao.ManagerDao;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.util.ServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.criteria.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:管理员管理
 */
@Service
public class ManagerServiceImpl implements ManagerService {

    private final Logger logger = LoggerFactory.getLogger(ManagerServiceImpl.class);

    @Inject
    private ManagerDao managerDao;
    @Override
    public ManagerPo findOne(Long managerId) {
        return managerDao.findOne(managerId);
    }

    @Override
    public ManagerPo findByPhone(String phone) {
        return managerDao.findByPhone(phone);
    }

    @Override
    public ManagerPo findByAccessKeyId(String AccessKeyId) {
        return managerDao.findByAccessKey(AccessKeyId);
    }

    @Override
    public ManagerPo findByUuid(String uuid) {
        return managerDao.findByUuid(uuid);
    }

    @Override
    public ManagerPo save(ManagerPo ManagerPo) {
        return managerDao.save(ManagerPo);
    }

    @Override
    public void delete(Long id) {
        managerDao.delete(id);
    }

    @Override
    public void deleteAll() {
        managerDao.deleteAll();
    }

    @Override
    public ManagerPo validateUser(String phone, String password) {
        return managerDao.findByPhoneAndPassword(phone,password);
    }

    @Override
    public List<ManagerPo> findAll() {
        return (List<ManagerPo>) managerDao.findAll();
    }

    @Override
    public List<ManagerPo> findAllByManager(ManagerPo managerPo) {
        switch (managerPo.getRolePo().getType()){
            case Test:
                return  (List<ManagerPo>) managerDao.findAll();
            case System:
                return  (List<ManagerPo>) managerDao.findAll();
            case Product:
                return managerDao.findByProductId(managerPo.getProductPo().getId());
            case Ordinary:

            default:
                return null;
        }
    }

    @Override
    public List<ManagerPo> findByProductId(Long productId) {
        return managerDao.findByProductId(productId);
    }

    @Override
    public List<ManagerPo> findAllByManager(ManagerPo managerPo, UserEnum.Status status) {
        switch (managerPo.getRolePo().getType()){
            case Test:
                return  (List<ManagerPo>) managerDao.findByStatus(status);
            case System:
                return  (List<ManagerPo>) managerDao.findByStatus(status);
            case Product:
                return managerDao.findByProductId(managerPo.getProductPo().getId(),status);
            case Ordinary:

            default:
                return null;
        }
    }

    @Override
    public Page<ManagerPo> findPage(final String type,final  String keyword,final  String param,final  String sort, PageRequest page) {
        return managerDao.findAll(new Specification<ManagerPo>() {
            public Predicate toPredicate(Root<ManagerPo> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

                Predicate p = null;
                Predicate status=null;
                List<Integer> list=new ArrayList<Integer>();
                list.add(0);
                list.add(1);
                list.add(2);
                Expression<String> exp = root.get("status");
                status=builder.and((exp.in(list)));

                if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(type)) {
                    //手机号 模糊查询
                    if ("phone".equals(type)) {
                        p = builder.and(builder.like((Path) root.get("phone"), "%" + keyword +"%" ));
                    }
                    //状态
                    if("status".equals(type)){
                        if ("Enable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Enable.getValue()));
                        }
                        if ("Disable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Disable.getValue()));
                        }
                        if ("IsDelete".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.IsDelete.getValue()));
                        }
                       /* if ("Test".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Test.getValue()));
                        }*/
                    }
                    if("createDate".equals(type)) {
                        Path<Date> createTime = root.get("createDate");
                        Date beginTime = null;
                        Date endTime = null;
                        if (StringUtils.isNotBlank(keyword) && keyword.contains("|")) {
                            String[] keyArr = keyword.split("\\|");
                            try {
                                System.out.println("keyArr.length:" + keyArr.length);
                                if (keyArr.length >= 1) {
                                    if (StringUtils.isNotBlank(keyArr[0]))
                                        beginTime = DateUtils.parseDate(keyArr[0], DateUtils.DATE_FORMAT_DATEONLY);

                                    if (keyArr.length == 2) {
                                        if (StringUtils.isNotBlank(keyArr[1]))
                                            endTime = DateUtils.parseDate(keyArr[1], DateUtils.DATE_FORMAT_DATETIME);
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        if (beginTime != null && endTime != null) {
                            p = builder.or(builder.between(createTime, beginTime, endTime));
                        } else if (beginTime != null) {
                            p = builder.or(builder.greaterThanOrEqualTo(createTime, beginTime));
                        }
                    }
                }
                if(null != p){
                    query.where(p,status);
                }else {
                    query.where(status);
                }
                //执行排序
                ServiceHelper.sortQuery(root, query, builder, param, sort);
                return null;
            }

        },page);
    }


    @Override
    public Page<ManagerPo> findPage(final ManagerPo managerPo,final  String type,final  String keyword,final  String param,final  String sort, PageRequest page) {
        return managerDao.findAll(new Specification<ManagerPo>() {
            public Predicate toPredicate(Root<ManagerPo> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

                Predicate managerP=null;
                if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){
                    managerP = builder.and(builder.equal(root.get("productPo").get("id"), managerPo.getProductPo().getId()));
                }else if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Ordinary)){
                    return null;
                }
                Predicate status=null;
            /*    List<UserEnum.Status> list=new ArrayList<UserEnum.Status>();
                list.add(UserEnum.Status.Disable);
                list.add(UserEnum.Status.Enable);
                list.add(UserEnum.Status.NoActive);*/
                List<Integer> list=new ArrayList<Integer>();
                list.add(0);
                list.add(1);
                list.add(2);
                Expression<String> exp = root.get("status");
                status=builder.and((exp.in(list)));
                Predicate p = null;

                if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(type)) {
                    //手机号 模糊查询
                    if ("phone".equals(type)) {
                        p = builder.and(builder.like((Path) root.get("phone"), "%" + keyword +"%" ));
                    }
                    //状态
                    if("status".equals(type)){
                        if ("Enable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Enable.getValue()));
                        }
                        if ("Disable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Disable.getValue()));
                        }
                        if ("IsDelete".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.IsDelete.getValue()));
                        }
                       /* if ("Test".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Test.getValue()));
                        }*/
                    }
                    if("createDate".equals(type)) {
                        Path<Date> createTime = root.get("createDate");
                        Date beginTime = null;
                        Date endTime = null;
                        if (StringUtils.isNotBlank(keyword) && keyword.contains("|")) {
                            String[] keyArr = keyword.split("\\|");
                            try {
                                System.out.println("keyArr.length:" + keyArr.length);
                                if (keyArr.length >= 1) {
                                    if (StringUtils.isNotBlank(keyArr[0]))
                                        beginTime = DateUtils.parseDate(keyArr[0], DateUtils.DATE_FORMAT_DATEONLY);

                                    if (keyArr.length == 2) {
                                        if (StringUtils.isNotBlank(keyArr[1]))
                                            endTime = DateUtils.parseDate(keyArr[1], DateUtils.DATE_FORMAT_DATETIME);
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        if (beginTime != null && endTime != null) {
                            p = builder.or(builder.between(createTime, beginTime, endTime));
                        } else if (beginTime != null) {
                            p = builder.or(builder.greaterThanOrEqualTo(createTime, beginTime));
                        }
                    }
                }
                if(null == p && managerP != null){
                    query.where(managerP,status);
                }else if(p!=null && managerP==null) {
                    query.where(status,p);
                } else if(null != p){
                    query.where(managerP,status,p);
                }
                if(p!=null) {
                    query.where(status,p);
                }
                //执行排序
                ServiceHelper.sortQuery(root, query, builder, param, sort);
                return null;
            }

        },page);
    }

}
