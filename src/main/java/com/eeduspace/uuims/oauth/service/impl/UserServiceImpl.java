package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.model.user.ImportExcelResults;
import com.eeduspace.uuims.oauth.persist.dao.PageDao;
import com.eeduspace.uuims.oauth.persist.dao.ProductUserDao;
import com.eeduspace.uuims.oauth.persist.dao.UserDao;
import com.eeduspace.uuims.oauth.persist.dao.UserInfoDao;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum.Status;
import com.eeduspace.uuims.oauth.persist.po.*;
import com.eeduspace.uuims.oauth.response.UserResponse;
import com.eeduspace.uuims.oauth.service.AclService;
import com.eeduspace.uuims.oauth.service.EnterpriseUserService;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.eeduspace.uuims.oauth.util.ServiceHelper;
import com.eeduspace.uuims.oauth.util.UIDGenerator;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.criteria.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:用户管理
 */
@Service
public class UserServiceImpl implements UserService {

    @Inject
    private UserDao userDao;
    @Inject
    private UserInfoDao userInfoDao;
    @Inject
    private ProductUserDao productUserDao;
    @Inject
    private PageDao pageDao;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private EnterpriseUserService enterpriseUserService;
    @Inject
    private AclService aclService;
    @Inject
    private ManagerLogService managerLogService;
    
    
    @Override
    public UserPo findOne(Long userId) {
        return userDao.findOne(userId);
    }

    @Override
    public UserPo findByPhone(String phone) {
        return userDao.findByPhone(phone);
    }

    @Override
    public UserPo findBySchoolNumber(String schoolNumber) {
        return userDao.findBySchoolNumber(schoolNumber);
    }

    @Override
    public UserPo findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public UserPo findByAccessKeyId(String AccessKeyId) {
        return userDao.findByAccessKey(AccessKeyId);
    }

    @Override
    public UserPo findByUuid(String uuid) {
        return userDao.findByUuid(uuid);
    }

    @Override
    public UserPo save(UserPo userPo) {
        return userDao.save(userPo);
    }


    @Override
    @Transactional
    public UserPo create(UserModel model , Long managerId) {

        String accessKeyId = "CU" + Digest.md5Digest16(model.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
        // 验证 AccessKeyId是否已存在
   /*     UserPo ven = userService.findByAccessKeyId(accessKeyId);
        if (ven != null) {
            accessKeyId = "CU" + Digest.md5Digest16(model.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
        }*/
        String secretKey = Digest.md5Digest(model.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + accessKeyId);
        model.setAccessKey(accessKeyId);
        model.setSecretKey(secretKey);
        UserPo po=new UserPo();//TODO 进行clone 后期优化
        po.setUuid(UIDGenerator.getUUID());
        po.setPhone(model.getPhone());
        po.setAccessKey(accessKeyId);
        po.setSecretKey(secretKey);
        po.setStatus(UserEnum.Status.Enable);
        po.setPassword(model.getPassword());
        po.setPhone(model.getPhone());
        po.setEmail(model.getEmail());
        po.setSchoolNumber(model.getSchoolNumber());
        po.setRegisterProductId(model.getRegisterProductId());
        po = userDao.save(po);
        UserInfoPo userInfoPo=new UserInfoPo();
        userInfoPo.setUserPo(po);
        userInfoPo.setCreateType(UserEnum.CreateType.ManagerAdd);
        userInfoPo.setRegisterSource(SourceEnum.EquipmentType.Template);
        userInfoPo.setManagerId(managerId);
        userInfoDao.save(userInfoPo);
        if(po.getRegisterProductId()!=null){
            if(productUserDao.findByUserIdAndProductId(po.getId(), po.getRegisterProductId())==null) {
                ProductUserPo productUserPo = new ProductUserPo();
                productUserPo.setUserId(po.getId());
                productUserPo.setProductId(po.getRegisterProductId());
                productUserDao.save(productUserPo);
            }
        }
        return po;
    }

    @Override
    @Transactional
    public List<UserPo> createList(List<UserModel> userModels, Long managerId) {
        List<UserPo> userPos =new ArrayList<>();
        for(UserModel model:userModels){
          UserPo po=  this.create(model,managerId);
            userPos.add(po);
        }
/*        List<UserInfoPo> userInfoPos=new ArrayList<>();
        for(UserModel model:userModels){
            UserPo po=new UserPo();//TODO 进行clone 后期优化
            po.setUuid(UIDGenerator.getUUID());
            po.setPhone(model.getPhone());
            po.setAccessKey(model.getAccessKey());
            po.setSecretKey(model.getSecretKey());
            po.setStatus(UserEnum.Status.Enable);
            po.setPassword(model.getPassword());
            po.setPhone(model.getPhone());
            po.setEmail(model.getEmail());
            po.setSchoolNumber(model.getSchoolNumber());
            po.setRegisterProductId(model.getRegisterProductId());
            userPos.add(po);
        }
        userPos= (List<UserPo>) userDao.save(userPos);
        for(UserPo p:userPos){
            UserInfoPo userInfoPo=new UserInfoPo();
            userInfoPo.setUserPo(p);
            userInfoPo.setCreateType(UserEnum.CreateType.ManagerAdd);
            userInfoPo.setRegisterSource(SourceEnum.EquipmentType.Template);
            userInfoPo.setManagerId(managerId);
            userInfoPos.add(userInfoPo);
            if(p.getRegisterProductId()!=null){
                if(productUserDao.findByUserIdAndProductId(p.getId(), p.getRegisterProductId())==null) {
                    ProductUserPo productUserPo = new ProductUserPo();
                    productUserPo.setUserId(p.getId());
                    productUserPo.setProductId(p.getRegisterProductId());
                    productUserDao.save(productUserPo);
                }
            }
        }
        userInfoDao.save(userInfoPos);*/
        return userPos;
    }

    @Override
    @Transactional
    public UserPo save(UserPo userPo, UserInfoPo userInfoPo) {
        userInfoDao.save(userInfoPo);
        return userDao.save(userPo);
    }

    @Override
    @Transactional
    //FIXME 修改
    public UserPo save(UserPo userPo, EnterpriseUserPo enterpriseUserPo) {
        userPo=userDao.save(userPo);
        if(userPo.getRegisterProductId()!=null){
            if(productUserDao.findByUserIdAndProductId(userPo.getId(), userPo.getRegisterProductId())==null) {
                ProductUserPo productUserPo = new ProductUserPo();
                productUserPo.setUserId(userPo.getId());
                productUserPo.setProductId(userPo.getRegisterProductId());
                productUserDao.save(productUserPo);
            }
        }
        enterpriseUserPo.setUserId(userPo.getId());
        enterpriseUserService.save(enterpriseUserPo);
        return userPo;
    }

    @Override
    @Transactional
    public void delete(UserPo userPo) {
//        productUserDao.deleteByUserId(userPo.getId());
//        userInfoDao.deleteByUserId(userPo.getId());
        userPo.setStatus(Status.IsDelete);
        this.save(userPo);
    }

    /**
     * 验证供应商用户
     * @param phone
     * @param password
     * @return
     */
    @Override
    public UserPo validateUser(String phone, String password) {
        UserPo user = userDao.findByPhone(phone);
        if(null!=user){
            String pwd = user.getPassword();
            logger.debug(">>>>>>>>>>>>>>>>>>>>>> password :{} pwd:{}" , password, pwd);
            if(password.equals(pwd)){
                return user;
            }
        }
        return null;
    }

    @Override
    public List<UserPo> findAll() {
        return (List<UserPo>) userDao.findAll();
    }

    @Override
    public List<UserPo> findAllByManager(ManagerPo managerPo) {
        switch (managerPo.getRolePo().getType()){
            case Test:
                return  (List<UserPo>) userDao.findAll();
            case System:
                return  (List<UserPo>) userDao.findAll();
            case Product:
                return productUserDao.findByProductId(managerPo.getProductPo().getId());
            case Ordinary:

            default:
                return null;
        }
    }

    @Override
    public List<UserPo> findAllByManager(ManagerPo managerPo, UserEnum.Status status) {
        switch (managerPo.getRolePo().getType()){
            case Test:
                return  (List<UserPo>) userDao.findByStatus(status);
            case System:
                return  (List<UserPo>) userDao.findByStatus(status);
            case Product:
                return productUserDao.findByProductId(managerPo.getProductPo().getId(),status);
            case Ordinary:

            default:
                return null;
        }
    }

    @Override
    public Page<UserPo> findPage(final String type,final  String keyword,final  String param,final  String sort, PageRequest page) {
        return userDao.findAll(new Specification<UserPo>() {
            public Predicate toPredicate(Root<UserPo> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                Predicate status=null;
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
                    //创建类型
                    if ("createType".equals(type)) {
                        if ("ManagerAdd".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("createType"), UserEnum.CreateType.ManagerAdd.getValue()));
                        }
                        if ("TemplateAdd".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("createType"), UserEnum.CreateType.TemplateAdd.getValue()));
                        }
                    }
                    //状态
                    if("status".equals(type)){
                        if ("NoActive".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.NoActive.getValue()));
                        }
                        if ("Enable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Enable.getValue()));
                        }
                        if ("Disable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Disable.getValue()));
                        }
                        if ("IsDelete".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.IsDelete.getValue()));
                        }
                    /*    if ("Test".equals(keyword)) {
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
    public Page<UserPo> findPage(final ManagerPo managerPo,final  String type,final  String keyword,final  String param,final  String sort, PageRequest page) {
        return userDao.findAll(new Specification<UserPo>() {
            public Predicate toPredicate(Root<UserPo> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

//                List<UserPo> list = productUserDao.findByProductId(managerPo.getProductId());
                Predicate managerP=null;
                if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){
                    managerP = builder.and(builder.equal(root.get("registerProductId"), managerPo.getProductPo().getId()));
                }else if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Ordinary)){
                    return null;
                }
                Predicate status=null;
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
                    //创建类型
                    if ("createType".equals(type)) {
                        if ("ManagerAdd".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("createType"), UserEnum.CreateType.ManagerAdd.getValue()));
                        }
                        if ("TemplateAdd".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("createType"), UserEnum.CreateType.TemplateAdd.getValue()));
                        }
                    }
                    //状态
                    if("status".equals(type)){
                        if ("NoActive".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.NoActive.getValue()));
                        }
                        if ("Enable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Enable.getValue()));
                        }
                        if ("Disable".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.Disable.getValue()));
                        }
                        if ("IsDelete".equals(keyword)) {
                            p = builder.or(builder.equal(root.get("status"), UserEnum.Status.IsDelete.getValue()));
                        }
                    /*    if ("Test".equals(keyword)) {
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

    @Override
    public AuthPage findPage(ManagerPo managerPo, String type, String keyword, String param, String sort, int pageNo, int pageSize) throws Exception {
        List<UserModel> resultList = new ArrayList<>();

        String sql = GET_USER_SQL;
        Object[] objs = null;

        //todo 验证manager
        // 状态
        if(StringUtils.isNotBlank(type) && StringUtils.isNotBlank(keyword)){
            if("status".equals(type)){

                sql +=" and d.status='"+keyword+"' ";
            }
        }

        sql += " order by d.createDate desc";
        logger.debug(sql);
        AuthPage page = pageDao.findByPage(pageNo,pageSize,sql,objs);

        List list = page.getList();
        if(null != page.getList() && !list.isEmpty()){
            for (Object obj :list){
                Map map = (Map) obj;
                resultList.add(authConverter.toUserModel(map));
            }
        }

        page.setList(resultList);
        return page;
    }

    @Override
    public UserInfoPo findInfoByUserId(Long userId) {
        return userInfoDao.findInfoByUserId(userId);
    }

    @Override
    public ImportExcelResults addEmployeeByExcel(byte[] byteArray) {
        return null;
    }

    @Override
    public List<UserPo> findByProductId(Long id) {
        return userDao.findByProductId(id);
    }


    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String GET_USER_SQL = "select d.* from auth_user d where 1=1 ";
    
    
    @Override
	public List<UserPo> getByRegisterProductIdAndUuidIn(Long productId, List<String> uuids) {
		// TODO Auto-generated method stub
		return userDao.findByRegisterProductIdAndUuidIn(productId, uuids);
    }

	@SuppressWarnings("unused")
	@Override
	@Transactional
	public UserResponse deleteAllUser(UserModel userModel, ManagerPo managerPo,String requestId) {
		//定义的 SuccessSize为成功数量，FailSize失败数量
		 int SuccessSize = 0,FailSized = 0 ;
		 String openIds = userModel.getOpenId();
		 String[]  openId= openIds.split(",");
         for (int i = 0; i < openId.length; i++) {
	        UserPo userPo=  userDao.findByUuid(openId[i]);
	    //    Long id = userPo.getId();
	        
			if(userPo==null){
	            logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
	            FailSized++;
	            continue;
			}
	       // productUserDao.deleteByUserId(userPo.getId());
	        this.delete(userPo);
//	        if (null != userInfoDao.findInfoByUserId(id)) {
//		        userInfoDao.deleteByUserId(id);
//			}

//	        if (null!=productUserDao.findByUserIdAndProductId(userPo.getId(), null)) {
//		        productUserDao.deleteByUserId(userPo.getId());
//			}
	        
	        managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
	        SuccessSize++;
        }
         UserResponse userResponse = new UserResponse();
         userResponse.setBatchSuccessSize(SuccessSize);
         userResponse.setBatchFailSize(FailSized);
		return userResponse;
	}

	@Override
	@Transactional
	public UserResponse updateStatusUser(UserModel userModel, ManagerPo managerPo, String requestId,Status status) {
		//定义的 SuccessSize为成功数量，FailSize失败数量
		 int SuccessSize = 0,FailSized = 0 ;
		 String openIds = userModel.getOpenId();
		 String[]  openId= openIds.split(",");
        for (int i = 0; i < openId.length; i++) {
	         UserPo userPo=  userDao.findByUuid(openId[i]);
	         if(userPo==null){
	             logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
	             FailSized++;
		         continue;       
		     }
	         if(userPo.getStatus().equals(status)){
	             logger.error("requestId："+requestId+","+ResponseCode.STATE_INCORRECT.toString()+ParamName.STATUS.toString());
	             FailSized++;
		         continue;
	         }
	         userPo.setStatus(status);
	         userDao.save(userPo);
	         managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
	         SuccessSize++;
        }
        
        UserResponse userResponse = new UserResponse();
        userResponse.setBatchSuccessSize(SuccessSize);
        userResponse.setBatchFailSize(FailSized);
		return userResponse;
	}
}
