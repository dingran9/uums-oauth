package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.TokenResponse;
import com.eeduspace.uuims.oauth.service.*;
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

/**
 * Author: dingran
 * Date: 2015/11/20
 * Description:
 */
@Component
@Path(value = "/redis")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class RedisWs extends BaseWs{

    private final Logger logger = LoggerFactory.getLogger(RedisWs.class);
    private String requestId;
    private Gson gson=new Gson();

    @Inject
    private UserService userService;
    @Inject
    private AclService aclService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private TokenService tokenService;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private ManagerLogService managerLogService;
    @Inject
    private UserLogService userLogService;
    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        TokenModel tokenModel = gson.fromJson(requestBody, TokenModel.class);

        switch (ActionName.toEnum(action)) {
            case GET_TOKEN:
                return getTokenInRedis(tokenModel, managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.TOKEN.toString()))).build();
        }
    }

    /**
     * 获取token 只有管理员有操作权限
     * @param managerPo
     * @return
     */
    private Response getTokenInRedis(TokenModel tokenM,ManagerPo managerPo) {

        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            TokenResponse tokenResponse=new TokenResponse();
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            String token_ = redisClientTemplate.get(tokenM.getToken());
            if(StringUtils.isBlank(token_)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString()))).build();
            }
            TokenModel tokenModel=gson.fromJson(token_, TokenModel.class);
            if(tokenModel==null || StringUtils.isBlank(tokenModel.getOpenId())){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString()))).build();
            }
            tokenResponse.setTokenModel(tokenModel);
            baseResponse.setResult(tokenResponse);
            managerLogService.create(managerPo,null, LogActionEnum.GET_TOKEN.toString(), LogActionEnum.REDIS.toString(),true,managerPo.getProductPo(),null,tokenM.getEquipmentType(),requestId);

/*            if(userPo!=null){
                userLogService.create(userPo, LogActionEnum.GET_TOKEN.toString(), LogActionEnum.REDIS.toString(),true,null,null,null,requestId);
            }*/
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},getTokenInRedis Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.TOKEN.toString()))).build();
        }

    }

}
