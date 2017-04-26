package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.ValidateUtils;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.RemoteAddressModel;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.RemoteAddressPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.RemoteAddressResponse;
import com.eeduspace.uuims.oauth.service.AclService;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.ProductService;
import com.eeduspace.uuims.oauth.service.RemoteAddressService;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Author: dingran
 * Date: 2016/4/5
 * Description:
 */
@Component
@Path(value = "/remoteAddress")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class RemoteAddressWs extends BaseWs {

    private final Logger logger = LoggerFactory.getLogger(RemoteAddressWs.class);

    private Gson gson = new Gson();

    @Inject
    private AuthConverter authConverter;
    @Inject
    private RemoteAddressService remoteAddressService;
    @Inject
    private AclService aclService;
    @Inject
    private ManagerLogService managerLogService;
    @Inject
    private ProductService productService;

    @Override
    public Response dispatch(String action, String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        RemoteAddressModel remoteAddressModel = gson.fromJson(requestBody, RemoteAddressModel.class);

        switch (ActionName.toEnum(action)) {
            case CREATE:
                return create(remoteAddressModel, managerPo);
            case LIST:
                return list(remoteAddressModel, managerPo);
            case DESCRIBE:
                return describe(remoteAddressModel, managerPo);
            case UPDATE:
                return update(remoteAddressModel, managerPo);
            case DELETE:
                return delete(remoteAddressModel, managerPo);
            case VALIDATE:
                return validate(remoteAddressModel,managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
        }
    }

    /**
     * 新增
     * @param managerPo
     * @return
     */
    private Response create(RemoteAddressModel remoteAddressModel, ManagerPo managerPo) {

        if(remoteAddressModel.getProductId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
        }
        if(remoteAddressModel.getRemoteAddress()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REMOTE_ADDRESS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REMOTE_ADDRESS.toString()))).build();
        }
        if(remoteAddressModel.getName()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.NAME.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.NAME.toString()))).build();
        }
        // 验证数据格式
        if(!ValidateUtils.isIP(remoteAddressModel.getRemoteAddress())){
            logger.error("RemoteAddress create Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.REMOTE_ADDRESS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.REMOTE_ADDRESS.toString()))).build();
        }
        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            RemoteAddressPo po= remoteAddressService.findByAddress(remoteAddressModel.getRemoteAddress());
            if(po!=null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ResourceName.REMOTE_ADDRESS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
            }
            ProductPo productPo= productService.findByUuid(remoteAddressModel.getProductId());
            if(productPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.PRODUCT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
            }
            RemoteAddressPo remoteAddressPo=new RemoteAddressPo();
            remoteAddressPo.setProductPo(productPo);
            remoteAddressPo.setRemoteAddress(remoteAddressModel.getRemoteAddress());
            remoteAddressPo.setName(remoteAddressModel.getName());
            remoteAddressPo=remoteAddressService.create(remoteAddressPo);
            BaseResponse baseResponse = new BaseResponse(requestId);
            RemoteAddressResponse remoteAddressResponse = new RemoteAddressResponse();
            List<RemoteAddressPo> remoteAddressPos=remoteAddressService.findAll();
            remoteAddressResponse.setRemoteAddressModel(authConverter.fromRemoteAddressPo(remoteAddressPo));
            remoteAddressResponse.setCount(remoteAddressPos.size());
            baseResponse.setResult(remoteAddressResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.LIST.toString(), LogActionEnum.REMOTE_ADDRESS.toString(),true,managerPo.getProductPo(),null,remoteAddressModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listRemoteAddress Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
        }
    }
    /**
     * IP白名单列表
     * @param managerPo
     * @return
     */
    private Response list(RemoteAddressModel remoteAddressModel, ManagerPo managerPo) {
        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            RemoteAddressResponse remoteAddressResponse = new RemoteAddressResponse();
            List<RemoteAddressPo> remoteAddressPos=remoteAddressService.findAll();
            remoteAddressResponse.setRemoteAddressModelList(authConverter.fromRemoteAddressPos(remoteAddressPos));
            remoteAddressResponse.setCount(remoteAddressPos.size());
            baseResponse.setResult(remoteAddressResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.LIST.toString(), LogActionEnum.REMOTE_ADDRESS.toString(),true,managerPo.getProductPo(),null,remoteAddressModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listRemoteAddress Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
        }
    }

    /**
     * IP白名单详情
     * @param managerPo
     * @return
     */
    private Response describe(RemoteAddressModel remoteAddressModel, ManagerPo managerPo) {

        if(remoteAddressModel.getRemoteAddressId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REMOTE_ADDRESS_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REMOTE_ADDRESS_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            RemoteAddressPo remoteAddressPo=remoteAddressService.findByUuid(remoteAddressModel.getRemoteAddressId());
            if(remoteAddressPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.REMOTE_ADDRESS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            RemoteAddressResponse remoteAddressResponse = new RemoteAddressResponse();
            remoteAddressResponse.setRemoteAddressModel(authConverter.fromRemoteAddressPo(remoteAddressPo));
            baseResponse.setResult(remoteAddressResponse);
            managerLogService.create(managerPo,remoteAddressModel.getRemoteAddressId(), LogActionEnum.DESCRIBE.toString(), LogActionEnum.REMOTE_ADDRESS.toString(),true,managerPo.getProductPo(),null,remoteAddressModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},describeRemoteAddress Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
        }
    }

    /**
     * IP白名单更新
     * @param managerPo
     * @return
     */
    private Response update(RemoteAddressModel remoteAddressModel, ManagerPo managerPo) {

        if(remoteAddressModel.getRemoteAddressId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REMOTE_ADDRESS_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REMOTE_ADDRESS_ID.toString()))).build();
        }
        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            RemoteAddressPo remoteAddressPo=remoteAddressService.findByUuid(remoteAddressModel.getRemoteAddressId());
            if(remoteAddressPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.REMOTE_ADDRESS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            RemoteAddressResponse remoteAddressResponse = new RemoteAddressResponse();
            if(StringUtils.isNotBlank(remoteAddressModel.getName())){
                remoteAddressPo.setName(remoteAddressModel.getName());
            }
            if(StringUtils.isNotBlank(remoteAddressModel.getRemoteAddress())){
                //验证唯一性
                RemoteAddressPo po= remoteAddressService.findByAddress(remoteAddressModel.getRemoteAddress());
                if(po!=null && !po.getId().equals(remoteAddressPo.getId())){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ResourceName.REMOTE_ADDRESS.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
                }
                remoteAddressPo.setRemoteAddress(remoteAddressModel.getRemoteAddress());
            }
            remoteAddressPo= remoteAddressService.create(remoteAddressPo);
            remoteAddressResponse.setRemoteAddressModel(authConverter.fromRemoteAddressPo(remoteAddressPo));
            baseResponse.setResult(remoteAddressResponse);
            managerLogService.create(managerPo,remoteAddressModel.getRemoteAddressId(), LogActionEnum.UPDATE.toString(), LogActionEnum.REMOTE_ADDRESS.toString(),true,managerPo.getProductPo(),null,remoteAddressModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},updateRemoteAddress Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
        }
    }

    /**
     * IP白名单删除状态
     * @param managerPo
     * @return
     */
    private Response delete(RemoteAddressModel remoteAddressModel, ManagerPo managerPo) {

        if(remoteAddressModel.getRemoteAddressId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REMOTE_ADDRESS_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REMOTE_ADDRESS_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            RemoteAddressPo remoteAddressPo=remoteAddressService.findByUuid(remoteAddressModel.getRemoteAddressId());
            if(remoteAddressPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.REMOTE_ADDRESS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
            }
            
            remoteAddressService.delete(remoteAddressPo.getId());
            
            BaseResponse baseResponse = new BaseResponse(requestId);
            managerLogService.create(managerPo,remoteAddressModel.getRemoteAddressId(), LogActionEnum.DELETE.toString(), LogActionEnum.REMOTE_ADDRESS.toString(),true,managerPo.getProductPo(),null,remoteAddressModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteRemoteAddress Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
        }
    }
    /**
     * IP白名单验证状态
     * @param managerPo
     * @return
     */
    private Response validate(RemoteAddressModel remoteAddressModel, ManagerPo managerPo) {

        if(remoteAddressModel.getRemoteAddress()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REMOTE_ADDRESS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REMOTE_ADDRESS.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            //验证唯一性
            RemoteAddressPo po= remoteAddressService.findByAddress(remoteAddressModel.getRemoteAddress());
            if(StringUtils.isNotBlank(remoteAddressModel.getRemoteAddressId())){
                if(po!=null && (!po.getUuid().equals(remoteAddressModel.getRemoteAddressId()))){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ResourceName.REMOTE_ADDRESS.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
                }
            }else {
                if(po!=null){
                    logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ResourceName.REMOTE_ADDRESS.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
                }
            }
            BaseResponse baseResponse=new BaseResponse();
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteRemoteAddress Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.REMOTE_ADDRESS.toString()))).build();
        }
    }
}
