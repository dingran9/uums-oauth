package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.LogModel;
import com.eeduspace.uuims.oauth.persist.po.ManagerLogPo;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserLogPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.ManagerLogResponse;
import com.eeduspace.uuims.oauth.response.UserLogResponse;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Author: dingran
 * Date: 2016/1/4
 * Description:
 */
@Component
@Path(value = "/log")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class LogWs  extends BaseWs {

    private final Logger logger = LoggerFactory.getLogger(LoginWs.class);
    private String requestId;
    private Gson gson=new Gson();

    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerLogService managerLogService;


    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        logger.debug("lo--->"+requestBody);
        LogModel logModel = gson.fromJson(requestBody, LogModel.class);
        logger.debug("lo--->",gson.toJson(logModel));

        switch (ActionName.toEnum(action)) {
            case MANAGER_LOGS:
                return managerLogs(logModel, managerPo);
            case USER_LOGS:
                return userLogs(logModel, managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.ROLE.toString()))).build();
        }
    }

    private Response managerLogs(LogModel logModel, ManagerPo managerPo) {
        if (logModel.getPageNo()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ ParamName.PAGE_NO.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_NO.toString()))).build();
        }
        if (logModel.getPageSize()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PAGE_SIZE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_SIZE.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            ManagerLogResponse managerLogResponse = new ManagerLogResponse();

            //TODO 需要代码优化
            PageRequest request = new PageRequest(logModel.getPageNo(), logModel.getPageSize());
            Page<ManagerLogPo>  page= managerLogService.findPage(managerPo,logModel.getType(), logModel.getKeyword(),logModel.getParam(),logModel.getSort(), request);
            managerLogResponse.setManagerLogList(authConverter.fromManagerLogPos(page.getContent()));
            managerLogResponse.setTotalRecords(page.getTotalPages());
            managerLogResponse.setTotalShowRecords((int) page.getTotalElements());
            baseResponse.setResult(managerLogResponse);
            managerLogService.create(managerPo,null, ActionName.MANAGER_LOGS.toString(),LogActionEnum.MANAGER.toString() ,true,managerPo.getProductPo(),null,logModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }


    private Response userLogs(LogModel logModel, ManagerPo managerPo) {
        if (logModel.getPageNo()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ ParamName.PAGE_NO.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_NO.toString()))).build();
        }
        if (logModel.getPageSize()==null) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.PAGE_SIZE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PAGE_SIZE.toString()))).build();
        }
        try {
            BaseResponse baseResponse = new BaseResponse(requestId);
            UserLogResponse userLogResponse = new UserLogResponse();

            //TODO 需要代码优化
            PageRequest request = new PageRequest(logModel.getPageNo(), logModel.getPageSize());
            Page<UserLogPo>  page= userLogService.findPage(null,managerPo,logModel.getType(), logModel.getKeyword(),logModel.getParam(),logModel.getSort(), request);
            userLogResponse.setUserLogList(authConverter.fromUserLogPos(page.getContent()));
            userLogResponse.setTotalRecords(page.getTotalPages());
            userLogResponse.setTotalShowRecords((int) page.getTotalElements());
            baseResponse.setResult(userLogResponse);
            managerLogService.create(managerPo,null, ActionName.MANAGER_LOGS.toString(),LogActionEnum.MANAGER.toString() ,true,managerPo.getProductPo(),null,logModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listUser Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

}
