package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.RoleModel;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.RolePo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.RoleResponse;
import com.eeduspace.uuims.oauth.service.AclService;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.RoleService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/11/4
 * Description:
 */
@Component
@Path(value = "/role")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class RoleWs extends BaseWs{

    private final Logger logger = LoggerFactory.getLogger(RoleWs.class);

    private Gson gson = new Gson();

    @Inject
    private AuthConverter authConverter;
    @Inject
    private RoleService roleService;
    @Inject
    private AclService aclService;

    @Inject
    private ManagerLogService managerLogService;
    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        RoleModel roleModel = gson.fromJson(requestBody, RoleModel.class);

        switch (ActionName.toEnum(action)) {
            case LIST:
                return list(roleModel, managerPo);
            case DESCRIBE:
                return describe(roleModel, managerPo);
            case UPDATE:
                return update(roleModel, managerPo);
            case UPDATE_STATUS:
                return updateStatus(roleModel, managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.ROLE.toString()))).build();
        }
    }

    /**
     * 角色列表
     * @param managerPo
     * @return
     */
    private Response list(RoleModel roleModel, ManagerPo managerPo) {
        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            RoleResponse roleResponse = new RoleResponse();
            RoleEnum.Status state = null;
            if (StringUtils.isNotBlank(roleModel.getStatus())) {
                state = authConverter.converterRoleStatus(roleModel.getStatus());
            }
            List<RoleModel> roleModels = new ArrayList<>();
            if (state != null) {
                roleModels = authConverter.fromRolePos(roleService.findByStatus(state));
            } else {
                roleModels = authConverter.fromRolePos(roleService.findAll());
            }
            roleResponse.setRoleList(roleModels);
            roleResponse.setCount(roleModels.size());
            baseResponse.setResult(roleResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.LIST.toString(), LogActionEnum.ROLE.toString(),true,managerPo.getProductPo(),null,roleModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listRole Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.ROLE.toString()))).build();
        }
    }

    /**
     * 角色详情
     * @param managerPo
     * @return
     */
    private Response describe(RoleModel roleModel, ManagerPo managerPo) {

        if(roleModel.getRoleId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.ROLE_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ROLE_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            RolePo rolePo=roleService.findByUuid(roleModel.getRoleId());
            if(rolePo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.ROLE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.ROLE.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            RoleResponse roleResponse = new RoleResponse();
            roleResponse.setRoleModel(authConverter.fromRolePo(rolePo));
            baseResponse.setResult(roleResponse);
            managerLogService.create(managerPo,rolePo.getUuid(), LogActionEnum.DESCRIBE.toString(), LogActionEnum.ROLE.toString(),true,managerPo.getProductPo(),null,roleModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},describeRole Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.ROLE.toString()))).build();
        }
    }

    /**
     * 角色更新
     * @param managerPo
     * @return
     */
    private Response update(RoleModel roleModel, ManagerPo managerPo) {

        if(roleModel.getRoleId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.ROLE_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ROLE_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            RolePo rolePo=roleService.findByUuid(roleModel.getRoleId());
            if(rolePo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.ROLE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.ROLE.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            RoleResponse roleResponse = new RoleResponse();
            if(StringUtils.isNotBlank(roleModel.getName())){
                rolePo.setName(roleModel.getName());
            }
            if(StringUtils.isNotBlank(roleModel.getDescription())){
                rolePo.setDescription(roleModel.getDescription());
            }
            rolePo= roleService.save(rolePo);
            roleResponse.setRoleModel(authConverter.fromRolePo(rolePo));
            baseResponse.setResult(roleResponse);
            managerLogService.create(managerPo,rolePo.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.ROLE.toString(),true,managerPo.getProductPo(),null,roleModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},updateRole Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.ROLE.toString()))).build();
        }
    }

    /**
     * 角色更新状态
     * @param managerPo
     * @return
     */
    private Response updateStatus(RoleModel roleModel, ManagerPo managerPo) {

        if(roleModel.getRoleId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.ROLE_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ROLE_ID.toString()))).build();
        }
        if(StringUtils.isBlank(roleModel.getStatus())){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.STATUS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.STATUS.toString()))).build();
        }

        try {
            RoleEnum.Status status=authConverter.converterRoleStatus(roleModel.getStatus());
            if(status==null){
                logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.STATUS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.STATUS.toString()))).build();
            }
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            RolePo rolePo=roleService.findByUuid(roleModel.getRoleId());
            if(rolePo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.ROLE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.ROLE.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            RoleResponse roleResponse = new RoleResponse();
            rolePo.setStatus(status);
            rolePo= roleService.save(rolePo);
            roleResponse.setRoleModel(authConverter.fromRolePo(rolePo));
            baseResponse.setResult(roleResponse);
            managerLogService.create(managerPo,rolePo.getUuid(), LogActionEnum.UPDATE_STATUS.toString(), LogActionEnum.ROLE.toString(),true,managerPo.getProductPo(),null,roleModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},updateStatusRole Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.ROLE.toString()))).build();
        }
    }
}
