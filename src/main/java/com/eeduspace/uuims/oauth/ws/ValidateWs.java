package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.model.ManagerModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.ManagerResponse;
import com.eeduspace.uuims.oauth.response.UserResponse;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:父类实现认证
 */
@Component
@Path(value = "/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class ValidateWs extends BaseWs {

    private final Logger logger = LoggerFactory.getLogger(ValidateWs.class);

    private Gson gson = new Gson();

    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {

        try {
            //TODO 返回token
            if (userPo != null) {
                UserModel userModel = new UserModel();
                userModel.setId(userPo.getId());
                userModel.setPhone(userPo.getPhone());
                userModel.setOpenId(userPo.getUuid());
                BaseResponse baseResponse =new BaseResponse(requestId);
                UserResponse userResponse = new UserResponse();
                userResponse.setUserModel(userModel);
                baseResponse.setResult(userResponse);
                return Response.ok(gson.toJson(baseResponse)).build();

            } else if (managerPo != null) {
                ManagerModel managerModel = new ManagerModel();
                managerModel.setId(managerPo.getId());
                managerModel.setPhone(managerPo.getPhone());
                managerModel.setOpenId(managerPo.getUuid());
                BaseResponse baseResponse =new BaseResponse(requestId);
                ManagerResponse managerResponse = new ManagerResponse();
                managerResponse.setManagerModel(managerModel);
                baseResponse.setResult(managerResponse);
                return Response.ok(gson.toJson(baseResponse)).build();
            }

        } catch (Exception e) {
            logger.error("requestId：{},validate Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString()))).build();
        }
        logger.info("validate response {}", gson.toJson(BaseResponse.setResponse(requestId, ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString())));
        return Response.ok(gson.toJson(BaseResponse.setResponse(requestId, ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString()))).build();
    }
}
