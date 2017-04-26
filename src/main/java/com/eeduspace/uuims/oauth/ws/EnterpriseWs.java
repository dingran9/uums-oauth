package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.EnterpriseModel;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.EnterprisePo;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.EnterpriseResponse;
import com.eeduspace.uuims.oauth.service.AclService;
import com.eeduspace.uuims.oauth.service.EnterpriseService;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
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
 * Date: 2015/11/9
 * Description:
 */
@Component
@Path(value = "/enterprise")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class EnterpriseWs  extends BaseWs{

    private final Logger logger = LoggerFactory.getLogger(RoleWs.class);

    private Gson gson = new Gson();

    @Inject
    private AuthConverter authConverter;
    @Inject
    private EnterpriseService enterpriseService;
    @Inject
    private AclService aclService;
    @Inject
    private ManagerLogService managerLogService;

    @Override
    public Response dispatch(String action, String token,String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        EnterpriseModel enterpriseModel = gson.fromJson(requestBody, EnterpriseModel.class);

        switch (ActionName.toEnum(action)) {
            case LIST:
                return list(enterpriseModel, managerPo);
            case DESCRIBE:
                return describe(enterpriseModel, managerPo);
            case GET_KEY:
                return getKey(enterpriseModel, managerPo);

            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.ENTERPRISE.toString()))).build();
        }
    }


    /**
     * 第三方企业列表
     * @param managerPo
     * @return
     */
    private Response list(EnterpriseModel enterpriseModel, ManagerPo managerPo) {
        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("listEnterprise Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            EnterpriseResponse enterpriseResponse = new EnterpriseResponse();
            List<EnterpriseModel> enterpriseModels = authConverter.fromEnterprises(enterpriseService.findAll());
            enterpriseResponse.setEnterpriseList(enterpriseModels);
            enterpriseResponse.setCount(enterpriseModels.size());
            baseResponse.setResult(enterpriseResponse);
            managerLogService.create(managerPo,null, LogActionEnum.LIST.toString(), LogActionEnum.ENTERPRISE.toString(),true,managerPo.getProductPo(),null,enterpriseModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listEnterprise Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.ENTERPRISE.toString()))).build();
        }
    }

    /**
     * 第三方企业详情
     * @param managerPo
     * @return
     */
    private Response describe(EnterpriseModel enterpriseModel, ManagerPo managerPo) {

        if(enterpriseModel.getEnterpriseId()==null){
            logger.error("describeEnterprise Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.ENTERPRISE_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ENTERPRISE_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("describeEnterprise Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            EnterprisePo enterprisePo=enterpriseService.findOne(enterpriseModel.getEnterpriseId());
            if(enterprisePo==null){
                logger.error("describeEnterprise Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.ENTERPRISE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.ENTERPRISE.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            EnterpriseResponse enterpriseResponse = new EnterpriseResponse();
            enterpriseResponse.setEnterpriseModel(authConverter.fromEnterprisePo(enterprisePo));
            baseResponse.setResult(enterpriseResponse);
            managerLogService.create(managerPo,null, LogActionEnum.DESCRIBE.toString(), LogActionEnum.ENTERPRISE.toString(),true,managerPo.getProductPo(),null,enterpriseModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},describeEnterprise Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.ENTERPRISE.toString()))).build();
        }
    }
    private Response getKey(EnterpriseModel enterpriseModel, ManagerPo managerPo) {

        if (StringUtils.isBlank(enterpriseModel.getType())) {
            logger.error("getEnterpriseKey Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.TYPE.toString()))).build();
        }
        try {
            if(!aclService.isSystem(managerPo)){
                logger.error("getEnterpriseKey Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            SourceEnum.EnterpriseType type = authConverter.converterEquipmentType(enterpriseModel.getType());
            if (type == null) {
                logger.error("getEnterpriseKey Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.TYPE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.TYPE.toString()))).build();
            }
            EnterprisePo enterprisePo = enterpriseService.findByEnterpriseType(type);
            BaseResponse baseResponse = new BaseResponse(requestId);
            EnterpriseResponse enterpriseResponse = new EnterpriseResponse();
            enterpriseResponse.setEnterpriseModel(authConverter.fromEnterprisePo(enterprisePo));
            baseResponse.setResult(enterpriseResponse);
            
            managerLogService.create(managerPo,null, LogActionEnum.GET_KEY.toString(), LogActionEnum.ENTERPRISE.toString(),true,managerPo.getProductPo(),null,enterpriseModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},getEnterpriseKey Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();

        }
    }
}
