package com.eeduspace.uuims.oauth.ws;


import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.ValidateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.comm.util.base.json.GsonUtil;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.model.UserListModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.dao.ProductUserDao;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.*;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.UserListResponse;
import com.eeduspace.uuims.oauth.response.UserResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:用户管理
 */ 

@Component
@Path(value = "/user")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class UserWs extends BaseWs {

    private final Logger logger = LoggerFactory.getLogger(UserWs.class);

    private Gson gson = new Gson();

    @Inject
    private UserService userService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private AclService aclService;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private TokenService tokenService;
    @Inject
    private ProductService productService;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerLogService managerLogService;
    @Inject
    private ProductUserDao productUserDao;
    @Value("${oauth.token.expires}")
    private String expires;

    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
    	

    	UserModel userModel = gson.fromJson(requestBody, UserModel.class);
        switch (ActionName.toEnum(action)) {
/*            case LOGIN:
                return login(userModel);*/
            case CREATE:
                return create(userModel, managerPo);
            case LIST:
                return list(userModel, managerPo);
            case PAGE_LIST:
                return pageList(userModel, managerPo);
            case DESCRIBE:
                return describe(userModel, userPo, managerPo);
            case EDIT_PASSWORD:
                return editPassword(userModel, userPo);
            case RESET_PASSWORD:
                return resetPassword(userModel, managerPo);
            case UPDATE:
                return update(userModel, userPo, managerPo);
            case UPDATE_STATUS:
                return updateStatus(userModel, managerPo);
            case BATCH_UPDATE_STATUS:
                return batchUpdateStatus(userModel, managerPo);    
            case DELETE:
                return delete(userModel, managerPo);
            case BATCH_DELETE_USERS:
                return batchDeleteUsers(userModel, managerPo);
            case VALIDATE:
                return validate(userModel, userPo, managerPo);
            case LOGOUT:
                return logout(userModel, userPo);
            case ACTIVATION:
                return activation(userModel, managerPo);
            case DESCRIBE_BY_PHONE:
                return descriBebyPhone(userModel, managerPo);
            case SEARCH_BY_PHONE:
                return searchByPhone(userModel, managerPo);
            case CREATE_LIST:
                UserListModel listModel = null;
                listModel =  gson.fromJson(requestBody, UserListModel.class);
                return createList(listModel,managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.USER.toString()))).build();
        }
    }






	/**
     * 创建用户
     *
     * @param userModel
     * @param managerPo
     * @return
     */
    private Response create(UserModel userModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(userModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(userModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        // 验证数据格式
        if (!ValidateUtils.isMobile(userModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
        }

        //TODO 注册来源  注册应用等数据添加
        try {
            if(!aclService.isManager(managerPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            //手机号查重
            UserPo userPo = userService.findByPhone(userModel.getPhone());
            if (userPo != null) {
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+ParamName.PHONE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ParamName.PHONE.toString()))).build();
            }
            //验证学籍号
            if(StringUtils.isNotBlank(userModel.getSchoolNumber())){
                userPo =  userService.findBySchoolNumber(userModel.getSchoolNumber());
                if(userPo!=null){
                    logger.error("requestId：" + requestId + "," + ResponseCode.RESOURCE_DUPLICATE.toString() + "schoolNumber");
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(),"schoolNumber"))).build();

                }
            }

            //验证产品  TODO 验证
            ProductPo productPo=null;
            if(managerPo.getRolePo().getType() == RoleEnum.Type.System){
                if(StringUtils.isNotBlank(userModel.getProductType())){
                     productPo=productService.findByType(userModel.getProductType());
                }
                if(StringUtils.isNotBlank(userModel.getProductId())){
                     productPo= productService.findByUuid(userModel.getProductId());
                }
                if(productPo==null){
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
                }
                userModel.setRegisterProductId(productPo.getId());
            }else {
                userModel.setRegisterProductId(managerPo.getProductPo().getId());
            }
            String accessKeyId = "CU" + Digest.md5Digest16(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            // 验证 AccessKeyId是否已存在
            UserPo ven = userService.findByAccessKeyId(accessKeyId);
            if (ven != null) {
                accessKeyId = "CU" + Digest.md5Digest16(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            }
            String secretKey = Digest.md5Digest(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + accessKeyId);
            userModel.setAccessKey(accessKeyId);
            userModel.setSecretKey(secretKey);
            //TODO 注册时 是否返回token 即 免登陆
            userPo = userService.create(userModel, managerPo.getId());
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            userResponse.setUserModel(authConverter.fromUserPo(userPo, false, true));
            baseResponse.setResult(userResponse);
            managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.CREATE.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},createUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }

    /**
     * 分页列表
     * @param userModel
     * @param managerPo
     * @return
     */
    private Response pageList(UserModel userModel, ManagerPo managerPo){
        if (userModel.getPageNo()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PAGE_NO.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_NO.toString()))).build();
        }
        if (userModel.getPageSize()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PAGE_SIZE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_SIZE.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();

            //TODO 需要代码优化
            PageRequest request = new PageRequest(userModel.getPageNo(), userModel.getPageSize());
            if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Test) || managerPo.getRolePo().getType().equals(RoleEnum.Type.System) ){
                Page<UserPo> page = userService.findPage(userModel.getType(), userModel.getKeyword(),userModel.getParam(),userModel.getSort(), request);
                userResponse.setUserList(authConverter.fromUserPos(page.getContent(),false,false));
                userResponse.setTotalRecords(page.getTotalPages());
                userResponse.setTotalShowRecords((int) page.getTotalElements());
                baseResponse.setResult(userResponse);
            }else  if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){
                Page<UserPo> page = userService.findPage(managerPo, userModel.getType(), userModel.getKeyword(),userModel.getParam(),userModel.getSort(), request);
                userResponse.setUserList(authConverter.fromUserPos(page.getContent(),false,false));
                userResponse.setTotalRecords(page.getTotalPages());
                userResponse.setTotalShowRecords((int) page.getTotalElements());
                baseResponse.setResult(userResponse);
            }

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 用户列表
     * @param userModel
     * @param managerPo
     * @return
     */
    private Response list(UserModel userModel, ManagerPo managerPo) {
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            UserEnum.Status state = null;
            if (StringUtils.isNotBlank(userModel.getStatus())) {
                state = authConverter.converterStatus(userModel.getStatus());
            }
            List<UserModel> userModels = new ArrayList<>();
            if (state != null) {
                userModels = authConverter.fromUserPos(userService.findAllByManager(managerPo, state), false,false);
            } else {
                userModels = authConverter.fromUserPos(userService.findAllByManager(managerPo), false,false);
            }
            userResponse.setUserList(userModels);
            userResponse.setCount(userModels.size());
            baseResponse.setResult(userResponse);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 用户详情
     * @param userModel
     * @param userPo
     * @param managerPo
     * @return
     */
    private Response describe(UserModel userModel, UserPo userPo, ManagerPo managerPo) {

        try {
            //todo 验证权限
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            if (userPo != null) {
                userResponse.setUserModel(authConverter.fromUserPo(userPo,true,false));
            } else if (managerPo != null) {
                if (StringUtils.isBlank(userModel.getOpenId())) {
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
                }
                UserPo userPo1=  userService.findByUuid(userModel.getOpenId());
                if(userPo1==null){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
                }
                userResponse.setUserModel(authConverter.fromUserPo(userPo1,true,false));
            }

            baseResponse.setResult(userResponse);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},describe User Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 修改用户密码
     * @param userModel
     * @param userPo
     * @return
     */
    private Response editPassword(UserModel userModel, UserPo userPo) {
        if (StringUtils.isBlank(userModel.getOldPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OLD_PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OLD_PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(userModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            if(!userPo.getPassword().equals(userModel.getOldPassword())){
                logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.OLD_PASSWORD.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.OLD_PASSWORD.toString()))).build();
            }
            userPo.setPassword(userModel.getPassword());
            userService.save(userPo);
            userLogService.create(userPo, LogActionEnum.EDIT_PASSWORD.toString(), LogActionEnum.USER.toString(),true,null,null,null,requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},editPasswordUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 重置密码  管理员权限
     * @param userModel
     * @param managerPo
     * @return
     */
    private Response resetPassword(UserModel userModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(userModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if (StringUtils.isBlank(userModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        try {

            BaseResponse baseResponse = new BaseResponse(requestId);
            UserPo userPo=  userService.findByUuid(userModel.getOpenId());
            if(userPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
            }
            //todo 验证权限
            if(!aclService.isHasPermission(managerPo,userPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            userPo.setPassword(userModel.getPassword());
            userService.save(userPo);
            managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.RESET_PASSWORD.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},resetPasswordUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 更新基本信息
     * @param userModel
     * @param userPo
     * @param managerPo
     * @return
     */
    private Response update(UserModel userModel, UserPo userPo, ManagerPo managerPo) {

    	try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            UserInfoPo userInfoPo=null;
            if (userPo != null) {
                userInfoPo=userService.findInfoByUserId(userPo.getId());
                if(userInfoPo==null){
                    userInfoPo=new UserInfoPo();
                    userInfoPo.setUserPo(userPo);
                }
            } else {
                if (StringUtils.isBlank(userModel.getOpenId())) {
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
                }
                userPo=userService.findByUuid(userModel.getOpenId());
                if(userPo==null){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
                }
                //todo 验证权限
                if(!aclService.isHasPermission(managerPo,userPo)){
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
                }
                userInfoPo=userService.findInfoByUserId(userPo.getId());
                if(userInfoPo==null){
                    userInfoPo=new UserInfoPo();
                    userInfoPo.setUserPo(userPo);
                }
            }
            if(StringUtils.isNotBlank(userModel.getPhone())){
                if(!ValidateUtils.isMobile(userModel.getPhone())){
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.PHONE.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
                }
                UserPo po= userService.findByPhone(userModel.getPhone());
                if(po!=null && !po.getUuid().equals(userModel.getOpenId())){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+ParamName.PHONE.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ParamName.PHONE.toString()))).build();
                }
                userPo.setPhone(userModel.getPhone());
            }
            if(StringUtils.isNotBlank(userModel.getEmail())){
                if(!ValidateUtils.isEmail(userModel.getEmail())){
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.EMAIL.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.EMAIL.toString()))).build();
                }
                userPo.setEmail(userModel.getEmail());
            }
            if(StringUtils.isNotBlank(userModel.getName())){
                userPo.setName(userModel.getName());
            }
            if(StringUtils.isNotBlank(userModel.getNickName())){
                userInfoPo.setNickName(userModel.getNickName());
            }
            if(StringUtils.isNotBlank(userModel.getAddress())){
                userInfoPo.setAddress(userModel.getAddress());
            }
            if(StringUtils.isNotBlank(userModel.getImagePath())){
                userInfoPo.setImagePath(userModel.getImagePath());
            }
            if(StringUtils.isNotBlank(userModel.getCardId())){
                userInfoPo.setCardId(userModel.getCardId());
            }
            if(StringUtils.isNotBlank(userModel.getRealName())){
                userInfoPo.setRealName(userModel.getRealName());
            }
            if(StringUtils.isNotBlank(userModel.getProvinceCode())){
                userInfoPo.setProvinceCode(userModel.getProvinceCode());
            }
            if(StringUtils.isNotBlank(userModel.getCityCode())){
                userInfoPo.setCityCode(userModel.getCityCode());
            }
            if(StringUtils.isNotBlank(userModel.getAreaCode())){
                userInfoPo.setAreaCode(userModel.getAreaCode());
            }
            if(StringUtils.isNotBlank(userModel.getStageCode())){
                userInfoPo.setStageCode(userModel.getStageCode());
            }
            if(StringUtils.isNotBlank(userModel.getGradeCode())){
                userInfoPo.setGradeCode(userModel.getGradeCode());
            }
            if(StringUtils.isNotBlank(userModel.getSchoolCode())){
                userInfoPo.setSchoolCode(userModel.getSchoolCode());
            }
            if(StringUtils.isNotBlank(userModel.getClassCode())){
                userInfoPo.setClassCode(userModel.getClassCode());
            }
            userPo= userService.save(userPo,userInfoPo);
            userResponse.setUserModel(authConverter.fromUserPo(userPo,true,false));
            baseResponse.setResult(userResponse);
            if(managerPo==null){
                userLogService.create(userPo, LogActionEnum.UPDATE.toString(), LogActionEnum.USER.toString(),true,null,null,userModel.getEquipmentType(),requestId);
            }else {
                managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
            }
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},updateUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    
    /**
     * 更新状态
     * @param userModel
     * @param managerPo
     * @return
     */
    private  Response updateStatus(UserModel userModel, ManagerPo managerPo){
        if (StringUtils.isBlank(userModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if (StringUtils.isBlank(userModel.getStatus())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.STATUS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.STATUS.toString()))).build();
        }
        UserEnum.Status status=authConverter.converterStatus(userModel.getStatus());
        if(status==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.STATUS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.STATUS.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserPo userPo=  userService.findByUuid(userModel.getOpenId());
            if(userPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
            }
            //todo 验证权限
            if(!aclService.isHasPermission(managerPo,userPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            if(userPo.getStatus().equals(status)){
                logger.error("requestId："+requestId+","+ResponseCode.STATE_INCORRECT.toString()+ParamName.STATUS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.STATE_INCORRECT.toString(), ParamName.STATUS.toString()))).build();
            }
            userPo.setStatus(status);
            userService.save(userPo);
            managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    
    /**
     * 批量更新状态
     * @param userModel
     * @param managerPo
     * @return
     */
    private  Response batchUpdateStatus(UserModel userModel, ManagerPo managerPo){
        if (StringUtils.isBlank(userModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if (StringUtils.isBlank(userModel.getStatus())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.STATUS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.STATUS.toString()))).build();
        }
        UserEnum.Status status=authConverter.converterStatus(userModel.getStatus());
        if(status==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.STATUS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.STATUS.toString()))).build();
        }
        if(!aclService.isSystem(managerPo)){
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = userService.updateStatusUser(userModel,managerPo,requestId,status);
            logger.debug("----成功导入用户数" + userResponse.getBatchSuccessSize() + "," + "失败数" + userResponse.getBatchFailSize());
            baseResponse.setResult(userResponse);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    /**
     * 删除
     * @param userModel
     * @param managerPo
     * @return
     */
    private Response delete(UserModel userModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(userModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        try {
            //todo 验证权限
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserPo userPo=  userService.findByUuid(userModel.getOpenId());
            if(userPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
            }
            //todo 验证权限
            if(!aclService.isHasPermission(managerPo,userPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            userService.delete(userPo);
            managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 批量删除
     * @param userModel
     * @param managerPo
     * @return
     */ 
    private Response batchDeleteUsers(UserModel userModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(userModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        try {
            //todo 验证权限
        	if(!aclService.isSystem(managerPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse =   userService.deleteAllUser(userModel,managerPo,requestId);
            logger.debug("----成功导入用户数" + userResponse.getBatchSuccessSize() + "," + "失败数" + userResponse.getBatchFailSize());
            baseResponse.setResult(userResponse);
            
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 验证
     * @param userModel
     * @param managerPo
     * @return
     */
    private Response validate(UserModel userModel, UserPo userPo,ManagerPo managerPo) {
        if (StringUtils.isBlank(userModel.getType())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.TYPE.toString()))).build();
        }

        try {

            BaseResponse baseResponse = new BaseResponse(requestId);
            UserPo po=null;
            if(userModel.getType().equals("phone")){
                if (StringUtils.isBlank(userModel.getPhone())) {
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PHONE.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
                }
                 po= userService.findByPhone(userModel.getPhone());

            }else if(userModel.getType().equals("email")){
                if (StringUtils.isBlank(userModel.getEmail())) {
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.EMAIL.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.EMAIL.toString()))).build();
                }
                 po= userService.findByEmail(userModel.getEmail());

            }else if(userModel.getType().equals("shcoolNumber")){
                if (StringUtils.isBlank(userModel.getSchoolNumber())) {
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.EMAIL.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.EMAIL.toString()))).build();
                }
                 po= userService.findBySchoolNumber(userModel.getSchoolNumber());
            }
            if(po==null){
                return Response.ok(gson.toJson(baseResponse)).build();
            }
            if(StringUtils.isNotBlank(userModel.getOpenId()) && po.getUuid().equals(userModel.getOpenId())){//已存在的账户 修改手机号
                return Response.ok(gson.toJson(baseResponse)).build();
            }
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.TYPE.toString()))).build();

        } catch (Exception e) {
            logger.error("requestId：{},validateUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    /**
     * 用户登录
     *
     * @param userModel 用户登录模型
     * @return
     */
    private Response login(UserModel userModel) {
        if (StringUtils.isBlank(userModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(userModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        // 验证数据格式
        if (!ValidateUtils.isMobile(userModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
        }
        try {
            UserPo userPo = userService.findByPhone(userModel.getPhone());
            if (userPo == null) {
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
            }
            if (!UserEnum.Status.Enable.equals(userPo.getStatus())) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            if (!userPo.getPassword().equals(userModel.getPassword())) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_AUTHFAILURE.toString()))).build();
            }

            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            UserModel model = authConverter.fromUserPo(userPo, true, false);
            //   token设置  设置刷新token  设置时效  设置loginCookie
            TokenModel tokenModel = new TokenModel();
            String token = "TK" + Digest.md5Digest16(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            String refreshToken = Digest.md5Digest(userModel.getPhone() + token);
            int expires_ = Integer.parseInt(expires);
            String loginCookie = Digest.md5Digest(userModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + token);

            model.setToken(token);
            model.setExpires(expires);
            model.setRefreshToken(refreshToken);
            model.setSessionId(loginCookie);

            tokenModel.setToken(token);
            tokenModel.setRefreshToken(refreshToken);
            tokenModel.setOpenId(userPo.getUuid());
            tokenModel.setExpires(expires);

            redisClientTemplate.setex(token, expires_, gson.toJson(tokenModel));

            TokenPo tokenPo=new TokenPo();
            tokenPo.setToken(token);
            tokenPo.setOpenId(userPo.getUuid());
            tokenPo.setRefreshToken(refreshToken);
            tokenPo.setExpires(expires);
            tokenService.save(tokenPo);

            userResponse.setUserModel(model);
            baseResponse.setResult(userResponse);
            userLogService.create(userPo, LogActionEnum.LOGIN.toString(), LogActionEnum.USER.toString(),true,null,null,userModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},User login Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    /**
     * 验证
     * @param userModel
     * @return
     */
    private Response logout(UserModel userModel, UserPo userPo) {

        //TODO 产品应用
        try {
            //TODO 删除 所有的logincookie
/*            userPo.setLoginStatus(UserEnum.LoginStatus.IsLogout);
            userPo.setUpdateTime(new Date());
            userService.save(userPo);*/
            userLogService.create(userPo, LogActionEnum.LOGOUT.toString(), LogActionEnum.USER.toString(),true,null,null,userModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ParamName.PHONE.toString()))).build();

        } catch (Exception e) {
            logger.error("requestId：{},validateUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    /**
     * 验证
     * @param userModel
     * @return
     */
    private Response activation(UserModel userModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(userModel.getPassword())) {
            logger.error("User activation Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(userModel.getPhone())) {
            logger.error("User activation Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        try {
            //TODO 验证管理员的权限
            if(managerPo==null){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
//            UserPo1 userPo=userService.validateUser(userModel.getPhone(),userModel.getPassword());
            UserPo userPo=userService.findByPhone(userModel.getPhone());
            if(userPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
            }
            if(!UserEnum.Status.Enable.equals(userPo.getStatus())){
                logger.error("User activation Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            if(!userPo.getPassword().equals(userModel.getPassword())){
                logger.debug("userPo.getPassword()="+userPo.getPassword());
                logger.debug("baseModel.getPassword()="+userModel.getPassword());
                logger.error("User activation Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_AUTHFAILURE.toString()))).build();
            }
            //todo 添加产品与用户的关系
            if(productUserDao.findByUserIdAndProductId(userPo.getId(), managerPo.getProductPo().getId())==null) {
                ProductUserPo productUserPo = new ProductUserPo();
                productUserPo.setUserId(userPo.getId());
                productUserPo.setProductId(userPo.getRegisterProductId());
                productUserDao.save(productUserPo);
            }

            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            userResponse.setUserModel(authConverter.fromUserPo(userPo, true, true));
            baseResponse.setResult(userResponse);
            managerLogService.create(managerPo,userPo.getUuid(), LogActionEnum.ACTIVATION.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,userModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},activation Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    
    /**
     * 根据手机号获取用户详情
     * @param userModel 
     * @param managerPo 
     * @return
     */
    private Response descriBebyPhone(UserModel userModel, ManagerPo managerPo) {
    	logger.debug("根据手机号获取用户详情");
    	
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            UserPo  userPo= null;
           if(!aclService.isManager(managerPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            if(managerPo != null) {
                if (StringUtils.isBlank(userModel.getPhone())) {
                	logger.error("User descriBebyPhone Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
                }
                userPo=  userService.findByPhone(userModel.getPhone());
                if(userPo==null){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.USER.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
                }
               // userResponse.setUserModel(authConverter.fromUserPo(userPo,true,false));
                userResponse.setUserModel(authConverter.fromUserByPhone(userPo));
            }
            baseResponse.setResult(userResponse);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},descriBebyPhone User Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    /**
     * 目前：智慧陪伴专用
     * 根据手机号获取用户详情
     *    存在  返回用户信息
     *    不存在 添加并返回
     * @param userModel 
     * @param managerPo 
     * @return
     */
    private Response searchByPhone(UserModel userModel, ManagerPo managerPo) {
    	logger.debug("根据手机号获取用户详情，存在  返回用户信息，不存在 添加并返回");
    	try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserResponse userResponse = new UserResponse();
            UserPo  userPo= null;
           if(!aclService.isManager(managerPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            if(managerPo != null) {
                if (StringUtils.isBlank(userModel.getPhone())) {
                	logger.error("User searchByPhone Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
                }
                userPo=  userService.findByPhone(userModel.getPhone());
                ProductPo productPo=null;
                if(userPo==null){
                	//验证产品  验证
                    if(managerPo.getRolePo().getType() == RoleEnum.Type.System){
                        if(StringUtils.isNotBlank(userModel.getProductType())){
                             productPo=productService.findByType(userModel.getProductType());
                        }
                        if(StringUtils.isNotBlank(userModel.getProductId())){
                             productPo= productService.findByUuid(userModel.getProductId());
                        }
                        if(productPo==null){
                            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
                        }
                        userModel.setRegisterProductId(productPo.getId());
                    }else {
                        userModel.setRegisterProductId(managerPo.getProductPo().getId());
                    }
                    String password  = Digest.md5Digest(RandomUtils.getRandom(6));
                    userModel.setPassword(password);
                    userPo = userService.create(userModel, managerPo.getId());
                }
                userResponse.setUserModel(authConverter.fromUserByPhone(userPo));
            }
            baseResponse.setResult(userResponse);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},User  searchByPhone  Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
	}

    
    /**
     * 创建用户
     *
     * @param listModel
     * @param managerPo
     * @return
     */
    private Response createList(UserListModel listModel, ManagerPo managerPo) {
        //TODO 注册来源  注册应用等数据添加
        if(!aclService.isManager(managerPo)){
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
        }
        List<UserModel> userModelList=null;
        try {
            userModelList = GsonUtil.fromListJson(gson.toJson(listModel), "userModelList", "", new TypeToken<List<UserModel>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(userModelList==null|| userModelList.isEmpty()){
            return Response.ok(gson.toJson(new BaseResponse(requestId))).build();
        }
        if(userModelList.size()>1000){
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_LIMITEXCEEDED.toString()))).build();
        }
        //TODO 验证数据
        List<UserModel> inUsePhones=new ArrayList<>();
        List<UserModel> inUseSchoolNumber=new ArrayList<>();
        List<UserModel> invalidNumber=new ArrayList<>();
        List<UserModel> results=new ArrayList<>();
        try {
            // TODO 验证产品
            Long productId=null;
            if(managerPo.getRolePo().getType() == RoleEnum.Type.System){

                if(StringUtils.isNotBlank(listModel.getProductType())){
                    ProductPo productPo=productService.findByType(listModel.getProductType());
                    if(productPo==null){
                        logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ResourceName.PRODUCT.toString());
                        return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
                    }
                    productId=productPo.getId();
                }
                if(StringUtils.isNotBlank(listModel.getProductId())){
                    ProductPo productPo= productService.findByUuid(listModel.getProductId());
                    if(productPo==null){
                        return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
                    }
                    productId=productPo.getId();
                }
            }else {
                productId=managerPo.getProductPo().getId();
            }

            for(UserModel model:userModelList){
/*                if (StringUtils.isBlank(model.getPassword())) {
                    logger.error("requestId：" + requestId + "," + ResponseCode.PARAMETER_MISS.toString() + ParamName.PASSWORD.toString());
                    invalidNumber.add(model);
                    continue;
                }
                if (StringUtils.isBlank(model.getPhone())) {
                    logger.error("requestId：" + requestId + "," + ResponseCode.PARAMETER_MISS.toString() + ParamName.PHONE.toString());
                    invalidNumber.add(model);
                    continue;
                }
                // 验证数据格式
                if (!ValidateUtils.isMobile(model.getPhone())) {
                    logger.error("requestId：" + requestId + "," + ResponseCode.PARAMETER_INVALID.toString() + ParamName.PHONE.toString());
                    invalidNumber.add(model);
                    continue;
                }*/

                //手机号查重
                UserPo userPo = userService.findByPhone(model.getPhone());
                if (userPo != null) {
                    inUsePhones.add(model);
                    continue;
                }
                //验证学籍号
                if(StringUtils.isNotBlank(model.getSchoolNumber())){
                    userPo =  userService.findBySchoolNumber(model.getSchoolNumber());
                    if(userPo!=null){
                        inUseSchoolNumber.add(model);
                        continue;
                    }
                }

                model.setRegisterProductId(productId);
                results.add(model);
            }

            //FIXME 异步
            List<UserPo> userPos=  userService.createList(results, managerPo.getId());
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserListResponse userListResponse=new UserListResponse();
            userListResponse.setSuccessList(authConverter.fromUserPos(userPos, false, true));
            userListResponse.setInUsePhones(inUsePhones);
            userListResponse.setInUseSchoolNumber(inUseSchoolNumber);
            userListResponse.setInvalidNumber(invalidNumber);
            baseResponse.setResult(userListResponse);
            managerLogService.create(managerPo,"", LogActionEnum.CREATE.toString(), LogActionEnum.USER.toString(),true,managerPo.getProductPo(),null,listModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},createUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }
}
