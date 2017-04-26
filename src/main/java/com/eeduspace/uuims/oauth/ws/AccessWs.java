package com.eeduspace.uuims.oauth.ws;


import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.ManagerModel;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.TokenEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.TokenPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.ManagerResponse;
import com.eeduspace.uuims.oauth.response.TokenResponse;
import com.eeduspace.uuims.oauth.response.UserResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;


/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:key 管理
 */

@Component
@Path(value = "/access")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class AccessWs extends BaseWs {

    private final Logger logger = LoggerFactory.getLogger(AccessWs.class);

    private Gson gson = new Gson();

    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private TokenService tokenService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerLogService managerLogService;
    @Value("${oauth.token.expires}")
    private String expires;

    @Override
    public Response dispatch(String action, String token,String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        TokenModel tokenModel = gson.fromJson(requestBody, TokenModel.class);

        switch (ActionName.toEnum(action)) {
            case GET_KEY:
                return getKey(tokenModel,userPo, managerPo);
            case UPDATE_SECRET_KEY:
                return updateSecretKey(tokenModel,userPo, managerPo);
//            case REFRESH_TOKEN:
//                return refreshToken(tokenModel,userPo, managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     *
     * @param userPo
     * @param managerPo
     * @return
     */
    private Response getKey(TokenModel tokenModel,UserPo userPo,ManagerPo managerPo) {

        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            UserResponse userResponse=null;
            ManagerResponse managerResponse=null;
            if(userPo!=null){
                UserModel userModel= authConverter.fromUserPo(userPo, false, true);
                userResponse=new UserResponse();
                userResponse.setUserModel(userModel);
                baseResponse.setResult(userResponse);
                userLogService.create(userPo, LogActionEnum.GET_KEY.toString(), LogActionEnum.ACCESS.toString(),true,null,null,tokenModel.getEquipmentType(),requestId);
            }
            if(managerPo!=null){
                ManagerModel managerModel= authConverter.fromManagerPo(managerPo, true);
                managerResponse=new ManagerResponse();
                managerResponse.setManagerModel(managerModel);
                baseResponse.setResult(managerResponse);
                managerLogService.create(managerPo,null, LogActionEnum.GET_KEY.toString(), LogActionEnum.ACCESS.toString(),true,managerPo.getProductPo(),null,tokenModel.getEquipmentType(),requestId);

            }
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},getKey Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }

    /**
     *更新秘钥
     * @param userPo
     * @param managerPo
     * @return
     */
    private Response updateSecretKey(TokenModel tokenModel,UserPo userPo,ManagerPo managerPo) {
        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            UserResponse userResponse=null;
            ManagerResponse managerResponse=null;
            if(userPo!=null){

                String secretKey = Digest.md5Digest(userPo.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + userPo.getAccessKey());
                userPo.setSecretKey(secretKey);
                userPo.setUpdateDate(new Date());
                userPo= userService.save(userPo);
                UserModel userModel= authConverter.fromUserPo(userPo, false, true);
                userResponse=new UserResponse();
                userResponse.setUserModel(userModel);
                baseResponse.setResult(userResponse);
                userLogService.create(userPo, LogActionEnum.UPDATE_SECRET_KEY.toString(), LogActionEnum.ACCESS.toString(),true,null,null,tokenModel.getEquipmentType(),requestId);

            }
            if(managerPo!=null){

                String secretKey = Digest.md5Digest(managerPo.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + managerPo.getAccessKey());
                managerPo.setSecretKey(secretKey);
                managerPo.setUpdateDate(new Date());
                managerPo= managerService.save(managerPo);
                ManagerModel managerModel= authConverter.fromManagerPo(managerPo, true);
                managerResponse=new ManagerResponse();
                managerResponse.setManagerModel(managerModel);
                baseResponse.setResult(managerResponse);
                managerLogService.create(managerPo,null, LogActionEnum.UPDATE_SECRET_KEY.toString(), LogActionEnum.ACCESS.toString(),true,managerPo.getProductPo(),null,tokenModel.getEquipmentType(),requestId);

            }

            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},updateSecretKey Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }

    /**
     * 刷新token
     * @param userPo
     * @param managerPo
     * @return
     */
    @Transactional
    private Response refreshToken(TokenModel tokenModel,UserPo userPo,ManagerPo managerPo) {

        try {
            TokenPo po= tokenService.findByRefreshToken(tokenModel.getRefreshToken());
            if(po==null){
                logger.error("refreshToken Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.REFRESH_TOKEN.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.REFRESH_TOKEN.toString()))).build();
            }
            String token="";
            String refreshToken="";
            int expires_=Integer.parseInt(expires);


            BaseResponse baseResponse=new BaseResponse(requestId);
            TokenPo tokenPo=new TokenPo();
            TokenModel model=new TokenModel();
            if(userPo!=null){
                if(!userPo.getUuid().equals(po.getOpenId())){
                    logger.error("refreshToken Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENFAILURE.toString()+"."+ResourceName.REFRESH_TOKEN.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_TOKENFAILURE.toString(), ResourceName.REFRESH_TOKEN.toString()))).build();
                }
                token="TK"+ Digest.md5Digest16(userPo.getPhone() +  DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
                refreshToken= Digest.md5Digest(userPo.getPhone() + token);
                tokenPo.setOpenId(userPo.getUuid());
                tokenPo.setType(TokenEnum.Type.User);
                model.setOpenId(userPo.getUuid());
                model.setType(TokenEnum.Type.User.toString());
            }
            if(managerPo!=null){
                if(!managerPo.getUuid().equals(po.getOpenId())){
                    logger.error("refreshToken Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENFAILURE.toString()+"."+ResourceName.REFRESH_TOKEN.toString());
                    return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_TOKENFAILURE.toString(), ResourceName.REFRESH_TOKEN.toString()))).build();
                }
                token="TK"+ Digest.md5Digest16(managerPo.getPhone() +  DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
                refreshToken= Digest.md5Digest(managerPo.getPhone() + token);
                tokenPo.setOpenId(managerPo.getUuid());
                tokenPo.setType(TokenEnum.Type.Manager);
                model.setOpenId(managerPo.getUuid());
                model.setType(TokenEnum.Type.Manager.toString());

            }
            redisClientTemplate.del(po.getToken());
            redisClientTemplate.setex(token, expires_, gson.toJson(tokenModel));

            model.setToken(token);
            model.setRefreshToken(refreshToken);
            model.setExpires(expires);

            tokenPo.setToken(token);
            tokenPo.setRefreshToken(refreshToken);
            tokenPo.setExpires(expires);
            tokenPo=tokenService.save(tokenPo,po);
            TokenResponse tokenResponse =new TokenResponse();
            tokenResponse.setTokenModel(authConverter.fromTokenPo(tokenPo));
            baseResponse.setResult(tokenResponse);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},refreshToken Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }
}

