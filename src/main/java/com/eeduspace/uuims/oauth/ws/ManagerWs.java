package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.ValidateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.ManagerModel;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.RolePo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.ManagerResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:管理员
 */
@Component
@Path(value = "/manager")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class ManagerWs extends BaseWs {


    private final Logger logger = LoggerFactory.getLogger(ManagerWs.class);

    private Gson gson = new Gson();

    @Inject
    private ManagerService managerService;
    @Inject
    private AclService aclService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private RoleService roleService;
    @Inject
    private ProductService productService;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private ManagerLogService managerLogService;
/*    @Inject
    private TokenService tokenService;*/
    @Value("${oauth.token.expires}")
    private String expires;
    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        ManagerModel managerModel = gson.fromJson(requestBody, ManagerModel.class);

        switch (ActionName.toEnum(action)) {
/*            case LOGIN:
                return login(managerModel);*/
            case CREATE:
                return create(managerModel, managerPo);
            case LIST:
                return list(managerModel, managerPo);
            case PAGE_LIST:
                return pageList(managerModel, managerPo);
            case DESCRIBE:
                return describe(managerModel, managerPo);
            case EDIT_PASSWORD:
                return editPassword(managerModel, managerPo);
            case RESET_PASSWORD:
                return resetPassword(managerModel, managerPo);
            case UPDATE:
                return update(managerModel, managerPo);
            case UPDATE_STATUS:
                return updateStatus(managerModel, managerPo);
            case DELETE:
                return delete(managerModel, managerPo);
            case VALIDATE:
                return validate(managerModel, managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }


    /**
     * 创建管理员
     *
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response create(ManagerModel managerModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(managerModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(managerModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        if(StringUtils.isBlank(managerModel.getRoleType())){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.ROLE_TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ROLE_TYPE.toString()))).build();
        }
/*        if(StringUtils.isBlank(managerModel.getProductId())){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
        }*/
        // 验证数据格式
        if (!ValidateUtils.isMobile(managerModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
        }
        try {
            if(!aclService.isManager(managerPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            //手机号查重
            ManagerPo manager = managerService.findByPhone(managerModel.getPhone());
            if (manager != null) {
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ParamName.PHONE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ParamName.PHONE.toString()))).build();
            }

            //验证角色
            RoleEnum.Type roleType=null;
            RolePo rolePo= roleService.findByType(authConverter.converterRoleType(managerModel.getRoleType()));
            if(rolePo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.ROLE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.ROLE.toString()))).build();
            }
            manager = new ManagerPo();
            //验证产品  TODO 验证
            if(managerPo.getRolePo().getType() == RoleEnum.Type.System) {

                if (StringUtils.isNotBlank(managerModel.getProductType())) {
                    ProductPo productPo = productService.findByType(managerModel.getProductType());
                    if (productPo == null) {
                        return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
                    }
                    manager.setProductPo(productPo);
                }
                if (StringUtils.isNotBlank(managerModel.getProductId())) {
                    ProductPo productPo = productService.findByUuid(managerModel.getProductId());
                    if (productPo == null) {
                        logger.error("requestId：" + requestId+"," + ResponseCode.RESOURCE_NOTFOUND.toString() + "." + ResourceName.PRODUCT.toString());
                        return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
                    }
                    manager.setProductPo(productPo);
                }
            }else {
                manager.setProductPo(managerPo.getProductPo());
            }
            manager.setPassword(managerModel.getPassword());
            manager.setPhone(managerModel.getPhone());
            String accessKeyId = "VE" + Digest.md5Digest16(managerModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            // 验证 AccessKeyId是否已存在
            ManagerPo ven = managerService.findByAccessKeyId(accessKeyId);
            if (ven != null) {
                accessKeyId = "VE" + Digest.md5Digest16(managerModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            }
            String secretKey = Digest.md5Digest(managerModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + accessKeyId);
            manager.setAccessKey(accessKeyId);
            manager.setSecretKey(secretKey);
            if (managerPo != null) {
                manager.setCreateManagerId(managerPo.getId());
                if(managerPo.getProductPo()!=null){
                    manager.setProductPo(managerPo.getProductPo());
                }
            }

            manager.setStatus(UserEnum.Status.Enable);
            manager.setRolePo(rolePo);

            manager = managerService.save(manager);
            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerResponse managerResponse = new ManagerResponse();
            managerResponse.setManagerModel(authConverter.fromManagerPo(manager, true));
            baseResponse.setResult(managerResponse);

            managerLogService.create(managerPo,manager.getUuid(), LogActionEnum.CREATE.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},createManager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }

    /**
     * 列表
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response list(ManagerModel managerModel, ManagerPo managerPo) {
        try {

            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerResponse managerResponse = new ManagerResponse();

            UserEnum.Status state = null;
            if (StringUtils.isNotBlank(managerModel.getStatus())) {
                state = authConverter.converterStatus(managerModel.getStatus());
            }
            List<ManagerModel> managerModels = new ArrayList<>();
            if (state != null) {
                managerModels = authConverter.fromManagerPos(managerService.findAllByManager(managerPo, state), false);
            } else {
                managerModels = authConverter.fromManagerPos(managerService.findAllByManager(managerPo), false);
            }
            managerResponse.setManagerList(managerModels);
            baseResponse.setResult(managerResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.LIST.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},createManager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }
    private Response pageList(ManagerModel managerModel, ManagerPo managerPo){
        if (managerModel.getPageNo()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PAGE_NO.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_NO.toString()))).build();
        }
        if (managerModel.getPageSize()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PAGE_SIZE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_SIZE.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerResponse managerResponse = new ManagerResponse();

            //TODO 需要代码优化
            PageRequest request = new PageRequest(managerModel.getPageNo(), managerModel.getPageSize());
            if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Test) || managerPo.getRolePo().getType().equals(RoleEnum.Type.System) ){
                Page<ManagerPo> page = managerService.findPage(managerModel.getType(), managerModel.getKeyword(),managerModel.getParam(),managerModel.getSort(), request);
                managerResponse.setManagerList(authConverter.fromManagerPos(page.getContent(), false));
                managerResponse.setTotalRecords(page.getTotalPages());
                managerResponse.setTotalShowRecords((int) page.getTotalElements());
                baseResponse.setResult(managerResponse);
            }else  if(managerPo.getRolePo().getType().equals(RoleEnum.Type.Product)){
                Page<ManagerPo> page = managerService.findPage(managerPo, managerModel.getType(), managerModel.getKeyword(),managerModel.getParam(),managerModel.getSort(), request);
                managerResponse.setManagerList(authConverter.fromManagerPos(page.getContent(), false));
                managerResponse.setTotalRecords(page.getTotalPages());
                managerResponse.setTotalShowRecords((int) page.getTotalElements());
                baseResponse.setResult(managerResponse);
            }else {
                List<ManagerPo> list=new ArrayList<>();
                list.add(managerPo);
                managerResponse.setManagerList(authConverter.fromManagerPos(list,false));
                managerResponse.setTotalRecords(1);
                managerResponse.setTotalShowRecords(1);
                baseResponse.setResult(managerResponse);
            }
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.PAGE_LIST.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    /**
     * 详情
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response describe(ManagerModel managerModel, ManagerPo managerPo) {
        try {
            //todo 验证权限
            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerResponse managerResponse = new ManagerResponse();
            if (StringUtils.isBlank(managerModel.getOpenId()) || managerPo.getUuid().equals(managerModel.getOpenId())) {
                managerResponse.setManagerModel(authConverter.fromManagerPo(managerPo, false));
            } else {
                ManagerPo po=  managerService.findByUuid(managerModel.getOpenId());
                if(po==null){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
                }
                Boolean isAll=false;
                if(aclService.isSystem(managerPo)){
                    isAll=true;
                }
                managerResponse.setManagerModel(authConverter.fromManagerPo(po, isAll));
            }

            baseResponse.setResult(managerResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.DESCRIBE.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},describe manager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }

    /**
     * 修改密码
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response editPassword(ManagerModel managerModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(managerModel.getOldPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.OLD_PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OLD_PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(managerModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            if(!managerPo.getPassword().equals(managerModel.getOldPassword())){
                logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.OLD_PASSWORD.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.OLD_PASSWORD.toString()))).build();
            }
            managerPo.setPassword(managerModel.getPassword());
            managerService.save(managerPo);
            managerLogService.create(managerPo,null, LogActionEnum.EDIT_PASSWORD.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,null,requestId);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.EDIT_PASSWORD.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},editPassword manager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }

    /**
     * 重置密码
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response resetPassword(ManagerModel managerModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(managerModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if (StringUtils.isBlank(managerModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        try {

            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerPo po=  managerService.findByUuid(managerModel.getOpenId());
            if(po==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
            }
            //todo 验证权限
            if(!aclService.isHasPermission(managerPo,po)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            po.setPassword(managerModel.getPassword());
            managerService.save(po);
            managerLogService.create(managerPo,po.getUuid(), LogActionEnum.RESET_PASSWORD.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},resetPasswordManager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }

    /**
     * 更新基本信息
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response update(ManagerModel managerModel, ManagerPo managerPo) {
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerResponse managerResponse = new ManagerResponse();
            ManagerPo po=null;

            if (StringUtils.isBlank(managerModel.getOpenId()) || managerPo.getUuid().equals(managerModel.getOpenId())) {
            //自己修改自己的信息
                po=managerPo;
            } else {//管理员修改其信息
                //todo 验证权限
                if (StringUtils.isBlank(managerModel.getOpenId())) {
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.OPENID.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
                }
                po=managerService.findByUuid(managerModel.getOpenId());
                if(po==null){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
                }
                //todo 验证权限
                if(!aclService.isHasPermission(managerPo,po)){
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
                }
            }
            if(StringUtils.isNotBlank(managerModel.getPhone())){
                if(!ValidateUtils.isMobile(managerModel.getPhone())){
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.PHONE.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
                }
                ManagerPo po1= managerService.findByPhone(managerModel.getPhone());
                if(po1!=null && !po1.getUuid().equals(managerModel.getOpenId())){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ParamName.PHONE.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ParamName.PHONE.toString()))).build();
                }
                po.setPhone(managerModel.getPhone());
            }
            if(StringUtils.isNotBlank(managerModel.getEmail())){
                if(!ValidateUtils.isEmail(managerModel.getEmail())){
                    logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.EMAIL.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.EMAIL.toString()))).build();
                }
                po.setEmail(managerModel.getEmail());
            }
            if(StringUtils.isNotBlank(managerModel.getName())){
                po.setName(managerModel.getName());
            }
            if(StringUtils.isNotBlank(managerModel.getProductId())){
                ProductPo productPo=  productService.findByUuid(managerModel.getProductId());
                if(productPo==null){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.PRODUCT.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
                }
                po.setProductPo(productPo);
            }
            po= managerService.save(po);
            managerResponse.setManagerModel(authConverter.fromManagerPo(po,false));
            baseResponse.setResult(managerResponse);
            managerLogService.create(managerPo,po.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},updateManager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }

    /**
     * 更新状态
     * @param managerModel
     * @param managerPo
     * @return
     */
    private  Response updateStatus(ManagerModel managerModel, ManagerPo managerPo){
        if (StringUtils.isBlank(managerModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if (StringUtils.isBlank(managerModel.getStatus())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.STATUS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.STATUS.toString()))).build();
        }
        UserEnum.Status status=authConverter.converterStatus(managerModel.getStatus());
        if(status==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.STATUS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.STATUS.toString()))).build();
        }
        try {
            //todo 验证权限
            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerPo po=  managerService.findByUuid(managerModel.getOpenId());
            if(po==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
            }
            //todo 验证权限
            if(!aclService.isHasPermission(managerPo,po) && po.getId() .equals( managerPo.getId())){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            if(po.getStatus().equals(status)){
                logger.error("requestId："+requestId+","+ResponseCode.STATE_INCORRECT.toString()+"."+ParamName.STATUS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.STATE_INCORRECT.toString(), ParamName.STATUS.toString()))).build();
            }
            po.setStatus(status);
            managerService.save(po);
            managerLogService.create(managerPo,po.getUuid(), LogActionEnum.UPDATE_STATUS.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteManager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }
    /**
     * 删除
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response delete(ManagerModel managerModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(managerModel.getOpenId())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        try {
            //todo 验证权限
            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerPo po=  managerService.findByUuid(managerModel.getOpenId());
            if(po==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
            }
            //todo 验证权限
            if(!aclService.isHasPermission(managerPo,po) && po.getId() .equals( managerPo.getId())){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            managerService.delete(po.getId());
            managerLogService.create(managerPo,po.getUuid(), LogActionEnum.DELETE.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteManager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }

    /**
     * 手机号验证
     * @param managerModel
     * @param managerPo
     * @return
     */
    private Response validate(ManagerModel managerModel, ManagerPo managerPo) {
        if (StringUtils.isBlank(managerModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        try {
            //todo 验证权限
            BaseResponse baseResponse = new BaseResponse(requestId);
            if(StringUtils.isNotBlank(managerModel.getOpenId())){
                ManagerPo po= managerService.findByPhone(managerModel.getPhone());
                if(po==null || po.getUuid().equals(managerModel.getOpenId())){
                    return Response.ok(gson.toJson(baseResponse)).build();
                }
            }else {
                ManagerPo po= managerService.findByPhone(managerModel.getPhone());
                if(po==null || managerPo.getPhone().equals(managerModel.getPhone())){
                    return Response.ok(gson.toJson(baseResponse)).build();
                }
            }
            logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ParamName.PHONE.toString());
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.DELETE.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ParamName.PHONE.toString()))).build();

        } catch (Exception e) {
            logger.error("requestId：{},validateManager Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }
    }

    /**
     * 管理员登录
     *
     * @param managerModel 管理员登录模型
     * @return
     */
    private Response login(ManagerModel managerModel) {
        if (StringUtils.isBlank(managerModel.getPassword())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(managerModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
/*        // 验证数据格式
        if (!ValidateUtils.isMobile(managerModel.getPhone())) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
        }*/
        try {
            ManagerPo managerPo = managerService.findByPhone(managerModel.getPhone());
            if (managerPo == null) {
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
            }
            if (!UserEnum.Status.Enable.equals(managerPo.getStatus())) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            if (!managerPo.getPassword().equals(managerModel.getPassword())) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_AUTHFAILURE.toString()))).build();
            }

            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerResponse managerResponse = new ManagerResponse();
            ManagerModel model = authConverter.fromManagerPo(managerPo, false);
            //   token设置  设置刷新token  设置时效  设置loginCookie
            TokenModel tokenModel=new TokenModel();
            String token="TK"+ Digest.md5Digest16(managerModel.getPhone() +  DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            String refreshToken= Digest.md5Digest(managerModel.getPhone() + token);
            int expires_=Integer.parseInt(expires);
            String loginCookie=Digest.md5Digest(managerModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + token);

            model.setToken(token);
            model.setExpires(expires);
            model.setRefreshToken(refreshToken);
            model.setSessionId(loginCookie);

            tokenModel.setToken(token);
            tokenModel.setRefreshToken(refreshToken);
            tokenModel.setOpenId(managerPo.getUuid());
            tokenModel.setExpires(expires);

  /*          TokenPo tokenPo=new TokenPo();
            tokenPo.setToken(token);
            tokenPo.setOpenId(managerPo.getUuid());
            tokenPo.setRefreshToken(refreshToken);
            tokenPo.setExpires(expires);
            tokenService.save(tokenPo);*/

            redisClientTemplate.setex(token, expires_, gson.toJson(tokenModel));

            managerResponse.setManagerModel(model);
            baseResponse.setResult(managerResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.MANAGER_LOGIN.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,managerModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},manager login Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.MANAGER.toString()))).build();
        }

    }

}
