package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.TokenEnum;
import com.eeduspace.uuims.oauth.persist.po.TokenPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.TokenResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.oauth.util.CommonUtil;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.eeduspace.uuims.oauth.util.UIDGenerator;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;

/**
 * Author: dingran
 * Date: 2015/11/11
 * Description:
 */
@Component
@Path(value = "/token")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class TokenWs{

    private final Logger logger = LoggerFactory.getLogger(TokenWs.class);
    private String requestId;
    private Gson gson=new Gson();

    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private TokenService tokenService;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerLogService managerLogService;
    @Value("${oauth.token.expires}")
    private String expires;
    @Value("${oauth.cookie.expires}")
    private String cookieExpires;
    @Value("${online.pidKey}")
	private String onlinePidKey;
	@Value("${online.uidKey}")
	private String onlineUidKey;
	
    @POST
    public Response process(
            @QueryParam("requestId") String requestId,
            @QueryParam("timestamp") String timestamp,
            @QueryParam("bodyMD5") String bodyMD5,
            @QueryParam("token") String token,
            @QueryParam("accessKey") String accessKey,
            @QueryParam("signature") String signature,
            @QueryParam("action") String action,
            String requestBody,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        //TODO  获取平台、终端等信息
        //进行基本的验证
        /**
         * 1.验证timestamp
         * 2.验证bodyMD5
         * 3.验证用户名密码*/

        this.requestId = requestId;
        if(StringUtils.isBlank(this.requestId)){
            this.requestId  = UIDGenerator.getUUID();
        }
        logger.info("requestId：{},timestamp：{},bodyMD5：{},token:{},action:{},requestBody:{}"
                ,this.requestId, timestamp, bodyMD5,token,action, requestBody );
        logger.info("HttpServletRequest: remoteAddr:{},ContextPath:{},RequestURI:{}", CommonUtil.getIpAddress(request),request.getContextPath(),request.getRequestURI());

        if (StringUtils.isBlank(bodyMD5)) {
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.BODY_MD5.toString()))).build();
        }
        if (StringUtils.isBlank(timestamp)) {
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.PARAMETER_MISS.toString(), ParamName.TIMESTAMP.toString()))).build();
        }
        long timestampDate = Long.parseLong(timestamp.trim());
        long currentDate = DateUtils.nowTimeMillis();

        long maxDate = currentDate + (1000 * 60 * 10);
        long minDate = currentDate - (1000 * 60 * 10);


        // 验证时间是否过期（超过请求时间戳前后半小时的请求为非法请求）
        if (timestampDate > maxDate || timestampDate < minDate) {
            logger.error("failed to access, timestamp expired.requestId：{}, timestamp:{}", this.requestId, timestamp);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.PARAMETER_INVALID.toString(),ParamName.TIMESTAMP.toString()))).build();
        }
        //验证请求body是否更改
        if (!Digest.md5Digest(requestBody).equals(bodyMD5)) {
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString(),ParamName.BODY_MD5.toString()))).build();
        }

        TokenModel tokenModel = gson.fromJson(requestBody, TokenModel.class);

        switch (ActionName.toEnum(action)) {
            case REFRESH_TOKEN:
                return refreshToken(tokenModel);
            case VALIDATE:
                return validateToken(token,tokenModel);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.TOKEN.toString()))).build();
        }
    }


/*    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        TokenModel tokenModel = gson.fromJson(requestBody, TokenModel.class);

        switch (ActionName.toEnum(action)) {
            case REFRESH_TOKEN:
                return refreshToken(tokenModel,managerPo,userPo);
            case VALIDATE:
                return validateToken(token,tokenModel,managerPo*//*,userPo*//*);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.TOKEN.toString()))).build();
        }
    }*/
    /**
     * 刷新token
     * @return
     */
    private Response refreshToken(TokenModel tokenModel) {
        if(tokenModel==null || StringUtils.isBlank(tokenModel.getRefreshToken())){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REFRESH_TOKE.toString()))).build();
        }

        try {
            TokenPo po= tokenService.findByRefreshToken(tokenModel.getRefreshToken());
            if(po==null){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.REFRESH_TOKEN.toString()))).build();
            }
            String token="";
            String refreshToken="";
            int expires_=Integer.parseInt(expires);


            BaseResponse baseResponse=new BaseResponse(requestId);
            TokenPo tokenPo=new TokenPo();
            TokenModel model=new TokenModel();

            token="TK"+ Digest.md5Digest16(po.getOpenId() +  DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            refreshToken= Digest.md5Digest(po.getOpenId() + token);

            model.setOpenId(po.getOpenId());
            model.setType(TokenEnum.Type.User.toString());
            model.setToken(token);
            model.setRefreshToken(refreshToken);
            model.setExpires(expires);

            redisClientTemplate.del(po.getToken());
            logger.debug("----------------------->"+gson.toJson(model));
            redisClientTemplate.setex(token, expires_, gson.toJson(model));

            tokenPo.setOpenId(po.getOpenId());
            tokenPo.setType(TokenEnum.Type.User);
            tokenPo.setToken(token);
            tokenPo.setRefreshToken(refreshToken);
            tokenPo.setExpires(expires);
            tokenPo=tokenService.save(tokenPo,po);
            TokenResponse tokenResponse =new TokenResponse();
            tokenResponse.setTokenModel(authConverter.fromTokenPo(tokenPo));
            baseResponse.setResult(tokenResponse);
            UserPo userPo=userService.findByUuid(po.getOpenId());
            if(userPo!=null){
                userLogService.create(userPo,LogActionEnum.REFRESH_TOKEN.toString(), LogActionEnum.TOKEN.toString(),true,po.getProductId(),null,tokenModel.getEquipmentType(),requestId);
            }

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},refreshToken Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.TOKEN.toString()))).build();
        }

    }


    /**
     * 单独验证token
     * @param token
     * @return
     */

    private Response validateToken(String token,TokenModel tokenM){

        if(tokenM==null || StringUtils.isBlank(tokenM.getOpenId())){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if (StringUtils.isBlank(token)) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.TOKEN.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.TOKEN.toString()))).build();
        }
        try{
            String token_ = redisClientTemplate.get(token);
            if(StringUtils.isBlank(token_)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENTIMEOUT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_TOKENTIMEOUT.toString()))).build();
            }
            TokenModel tokenModel=gson.fromJson(token_, TokenModel.class);
            if(tokenModel==null || StringUtils.isBlank(tokenModel.getOpenId())){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENFAILURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_TOKENFAILURE.toString()))).build();
            }
            if(!tokenM.getOpenId().equals(tokenModel.getOpenId())){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENFAILURE.toString()+ParamName.OPENID.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_AUTHFAILURE.toString(), ParamName.OPENID.toString()))).build();
            }
            //刷新sessionId，onlineUser，onlineProduct的缓存时间
            String onlineCookie = redisClientTemplate.get(tokenModel.getSessionId());
            if (StringUtils.isBlank(onlineCookie)) {
            	 logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+ParamName.TOKEN.toString());
            	 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.TOKEN.toString()))).build();
			}
            redisClientTemplate.expire(onlineCookie, Integer.parseInt(cookieExpires));
            
            String onlineUser = redisClientTemplate.get(MessageFormat.format(this.onlineUidKey, tokenModel.getOpenId()));
            if (StringUtils.isBlank(onlineUser)) {
            	 logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString() + ParamName.OPENID.toString());
            	 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
			}
            redisClientTemplate.expire(onlineUser, Integer.parseInt(cookieExpires));
            
            UserModel userModel=gson.fromJson(onlineUser, UserModel.class);
            String onlineProductKey = MessageFormat.format(this.onlinePidKey, userModel.getProductId(), tokenModel.getEquipmentType(),userModel.getOpenId());
            String onlineProduct = redisClientTemplate.get(onlineProductKey);
            if(StringUtils.isBlank(onlineProduct)){
            	 logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString() + ParamName.PRODUCT_ID.toString());
            	 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
            }
            redisClientTemplate.expire(onlineProduct, Integer.parseInt(cookieExpires));

            UserPo userPo=userService.findByUuid(tokenModel.getOpenId());
            if(userPo!=null) {
                userLogService.create(userPo, LogActionEnum.VALIDATE.toString(), LogActionEnum.TOKEN.toString(), true,tokenModel.getProductId(), null, tokenModel.getEquipmentType(), requestId);
            }
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SUCCESS.toString()))).build();
        } catch (Exception e) {
            logger.error("requestId：{},validateToken Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.TOKEN.toString()))).build();
        }
    }

}
