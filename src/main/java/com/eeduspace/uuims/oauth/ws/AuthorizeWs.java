package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.BaseModel;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.EnterpriseUserPo;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.TokenResponse;
import com.eeduspace.uuims.oauth.response.UserResponse;
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
import org.springframework.stereotype.Component;

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
 * Date: 2015/11/25
 * Description:
 */

@Component
@Path(value = "/authorize")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class AuthorizeWs extends BaseWs{

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
    private EnterpriseUserService enterpriseUserService;
    @Inject
    private ProductService productService;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerLogService managerLogService;
/*    @Inject
    private EnterpriseService enterpriseService;
    @Inject
    private EnterpriseTokenService enterpriseTokenService;

    @Value("${uuims.xl.appid}")
    private String xlAppid;
    @Value("${uuims.xl.appsecret}")
    private String xlAppsecret;
    @Value("${uuims.xl.redirect.uri}")
    private String xlRedirectUrl;
    @Value("${uuims.wx.appid}")
    private String wxAppid;
    @Value("${uuims.wx.appsecret}")
    private String wxAppsecret;
    @Value("${uuims.wx.redirect.uri}")
    private String wxRedirectUrl;
    @Value("${uuims.qq.appid}")
    private String qqAppid;
    @Value("${uuims.qq.appsecret}")
    private String qqAppsecret;
    @Value("${uuims.qq.redirect.uri}")
    private String qqRedirectUrl;*/

    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        BaseModel baseModel = gson.fromJson(requestBody, BaseModel.class);

        switch (ActionName.toEnum(action)) {
            case GET_TOKEN:
                return getTokenInRedis(baseModel, managerPo);
            case BIND_PHONE:
                return bindPhone(baseModel);
/*            case AUTHORIZE_SINA:
                return authorizeSina(code,redirectUri,baseModel);
            case AUTHORIZE_TENCENT:
                return authorizeTencent(code,redirectUri,baseModel);
            case AUTHORIZE_WECHAT:
                return authorizeWeChat(code,baseModel);*/
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     *
     * @param managerPo
     * @return
     */
    private Response getTokenInRedis(BaseModel baseModel,ManagerPo managerPo) {

        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            TokenResponse tokenResponse=new TokenResponse();
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            String token_ = redisClientTemplate.get(baseModel.getToken());
            if(StringUtils.isBlank(token_)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString()))).build();
            }
            TokenModel tokenModel=gson.fromJson(token_, TokenModel.class);
            if(tokenModel==null || StringUtils.isBlank(tokenModel.getOpenId())){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString()))).build();
            }
            tokenResponse.setTokenModel(tokenModel);
            baseResponse.setResult(tokenResponse);
            managerLogService.create(managerPo,null, LogActionEnum.GET_TOKEN.toString(), LogActionEnum.AUTHORIZE.toString(),true,managerPo.getProductPo(),null,null,requestId);

            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},getTokenInRedis Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }


    /**
     *
     * @param baseModel
     * @return
     */
    private Response bindPhone(BaseModel baseModel ) {
        if(StringUtils.isBlank(baseModel.getThirdOpenId())){
            logger.error("bindPhone Exception : requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if(StringUtils.isBlank(baseModel.getPhone())){
            logger.error("bindPhone Exception : requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        if(StringUtils.isBlank(baseModel.getProductId())){
            logger.error("authorize Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();

        }
        try {

            ProductPo productPo=productService.findByUuid(baseModel.getProductId());
            if(productPo==null){
                logger.error("bindPhone Exception: requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.PRODUCT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
            }

/*            EnterpriseUserPo po=enterpriseUserService.findByPhone(baseModel.getPhone());
            if(po != null){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(), ParamName.PHONE.toString()))).build();
            }*/
            EnterpriseUserPo enterpriseUserPo= enterpriseUserService.findByOpenId(baseModel.getThirdOpenId());
            if(enterpriseUserPo==null){
                logger.error("bindPhone Exception: requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ ResourceName.USER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
            }
            enterpriseUserPo.setPhone(baseModel.getPhone());
            enterpriseUserPo.setUpdateDate(new Date());
            UserPo userPo=new UserPo();
            userPo.setPhone(baseModel.getPhone());
            userPo.setRegisterProductId(productPo.getId());
            String accessKeyId = "CU" + Digest.md5Digest16(baseModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            // 验证 AccessKeyId是否已存在
            UserPo ven = userService.findByAccessKeyId(accessKeyId);
            if (ven != null) {
                accessKeyId = "CU" + Digest.md5Digest16(baseModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            }
            String secretKey = Digest.md5Digest(baseModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + accessKeyId);
            userPo.setAccessKey(accessKeyId);
            userPo.setSecretKey(secretKey);
            userPo.setStatus(UserEnum.Status.NoActive);
            //FIXME 修改
            userPo= userService.save(userPo,enterpriseUserPo);
            BaseResponse baseResponse=new BaseResponse(requestId);
            UserResponse userResponse=new UserResponse();
            userResponse.setUserModel(authConverter.fromUserPo(userPo,false,true));

            userLogService.create(userPo, LogActionEnum.BIND_PHONE.toString(), LogActionEnum.AUTHORIZE.toString(),true,null,null,baseModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("bindPhone Exceptionre :questId：{},bindPhone Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }


/*
    private Response authorizeWeChat(String code,BaseModel baseModel) {
        if(StringUtils.isBlank(code)){
            logger.error("authorize Exception：requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.CODE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.CODE.toString()))).build();
        }

        logger.info(" oauth appid:"+wxAppid+"  appsecret:"+wxAppsecret+"  code:"+code);

        try {
            EnterprisePo enterprisePo=enterpriseService.findByEnterpriseType(SourceEnum.EnterpriseType.WeChat);
            if(enterprisePo==null){
                enterprisePo=new EnterprisePo();
                enterprisePo.setAppId(wxAppid);
                enterprisePo.setAppKey(wxAppsecret);
                enterprisePo.setEnterpriseType(SourceEnum.EnterpriseType.WeChat);
                enterprisePo.setType(EnterpriseEnum.Type.B);
                enterprisePo=  enterpriseService.save(enterprisePo);
            }
            BaseResponse baseResponse=new BaseResponse(requestId);
            SNSUserResponse wxUserResponse=new SNSUserResponse();
            // 用户同意授权
            if (!"authdeny".equals(code)) {
                // 获取网页授权access_token
                Oauth2Token oauth2Token = WXAdvancedUtil.getOauth2AccessToken(wxAppid, wxAppsecret, code);
                if(oauth2Token==null){
                    return Response.ok(gson.toJson(baseResponse)).build();
                }
                // 网页授权接口访问凭证 此ak与普通调用接口使用的ak不同 ,详见:http://mp.enterprise.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
                String accessToken = oauth2Token.getAccessToken();
                // 用户标识
                String openId = oauth2Token.getOpenId();
                // 获取用户信息
                SNSUserInfo snsUserInfo = WXAdvancedUtil.getSNSUserInfo(accessToken, openId);

                if(snsUserInfo != null){
                    wxUserResponse.setSnsUserInfo(snsUserInfo);
                    baseResponse.setResult(wxUserResponse);
                    snsUserInfo.setScope(oauth2Token.getScope());
                    //TODO 授权成功后，做对应业务操纵后返回页面

                    EnterpriseUserPo enterpriseUserPo=new EnterpriseUserPo();
                    enterpriseUserPo.setEnterpriseId(enterprisePo.getId());
                    enterpriseUserPo.setOpenId(snsUserInfo.getThridOpenId());
                    enterpriseUserPo.setScope(snsUserInfo.getScope());
                    enterpriseUserPo.setNickName(snsUserInfo.getNickname());
                    enterpriseUserPo.setSex(authConverter.converterSex(snsUserInfo.getSex()));
                    enterpriseUserPo.setCountry(snsUserInfo.getCountry());
                    enterpriseUserPo.setCity(snsUserInfo.getCity());
                    enterpriseUserPo.setProvince(snsUserInfo.getProvince());
                    enterpriseUserPo.setHeadImgUrl(snsUserInfo.getHeadImgUrl());
                    enterpriseUserService.save(enterpriseUserPo);

                    //记录刷新令牌
          */
/*          EnterpriseTokenPo enterpriseTokenPo=new EnterpriseTokenPo();
                    enterpriseTokenPo.setOpenId(snsUserInfo.getOpenId());
                    enterpriseTokenPo.setExpires(oauth2Token.getExpiresIn().toString());
                    enterpriseTokenPo.setRefreshToken(oauth2Token.getRefreshToken());
                    enterpriseTokenPo.setToken(oauth2Token.getAccessToken());
                    enterpriseTokenService.save(enterpriseTokenPo);*//*


                    //TODO 跳转绑定手机号界面
                }
            }

            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},authorize Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }


    private Response authorizeSina(String code,String uri,BaseModel baseModel){
        if(StringUtils.isBlank(code)){
            logger.error("authorize Exception：requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.CODE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.CODE.toString()))).build();
        }
        if(StringUtils.isBlank(uri)){
            logger.error("authorize Exception：requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REDIRECT_URI.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REDIRECT_URI.toString()))).build();
        }
        logger.info(" oauth appid:"+xlAppid+"  appsecret:"+xlAppsecret+"  code:"+code+"  uri:"+uri);

        if(StringUtils.isBlank(baseModel.getProductId())){
            logger.error("authorize Exception：requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
        }
        try {

            EnterprisePo enterprisePo=enterpriseService.findByEnterpriseType(SourceEnum.EnterpriseType.Sina);
            if(enterprisePo==null){
                enterprisePo=new EnterprisePo();
                enterprisePo.setAppId(xlAppid);
                enterprisePo.setAppKey(xlAppsecret);
                enterprisePo.setEnterpriseType(SourceEnum.EnterpriseType.Sina);
                enterprisePo.setType(EnterpriseEnum.Type.B);
                enterprisePo=  enterpriseService.save(enterprisePo);
            }
            BaseResponse baseResponse=new BaseResponse(requestId);
            SNSUserResponse snsUserResponse=new SNSUserResponse();

            // 用户同意授权
            if (!"authdeny".equals(code)) {
                // 获取网页授权access_token
                Oauth2Token oauth2Token = SinaAdvancedUtil.getOauth2AccessToken(xlAppid, xlAppsecret, code, uri);
                // 网页授权接口访问凭证 此ak与普通调用接口使用的ak不同 ,详见:http://mp.enterprise.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
                String accessToken = oauth2Token.getAccessToken();
                // 用户标识
                String openId = oauth2Token.getOpenId();

                //TODO  获取用户基本信息
                SNSUserInfo snsUserInfo =new SNSUserInfo();
                snsUserInfo.setThridOpenId(openId);
                snsUserResponse.setSnsUserInfo(snsUserInfo);
                baseResponse.setResult(snsUserResponse);

                EnterpriseUserPo enterpriseUserPo=new EnterpriseUserPo();
                enterpriseUserPo.setEnterpriseId(enterprisePo.getId());
                enterpriseUserPo.setOpenId(openId);
                enterpriseUserService.save(enterpriseUserPo);
*/
/*                // 获取用户信息
                SNSUserInfo snsUserInfo = SinaAdvancedUtil.getSNSUserInfo(accessToken, openId , xlAppid);

                if(snsUserInfo != null){
                    snsUserInfo.setScope(oauth2Token.getScope());
                    wxUserResponse.setSnsUserInfo(snsUserInfo);
                    baseResponse.setResult(wxUserResponse);
                    //TODO 授权成功后，做对应业务操纵后返回页面

                    EnterpriseUserPo enterpriseUserPo=new EnterpriseUserPo();
                    enterpriseUserPo.setEnterpriseId(enterprisePo.getId());
                    enterpriseUserPo.setOpenId(snsUserInfo.getOpenId());
                    enterpriseUserService.save(enterpriseUserPo);

                    //记录刷新令牌
          *//*
*/
/*          EnterpriseTokenPo enterpriseTokenPo=new EnterpriseTokenPo();
                    enterpriseTokenPo.setOpenId(snsUserInfo.getOpenId());
                    enterpriseTokenPo.setExpires(oauth2Token.getExpiresIn().toString());
                    enterpriseTokenPo.setRefreshToken(oauth2Token.getRefreshToken());
                    enterpriseTokenPo.setToken(oauth2Token.getAccessToken());
                    enterpriseTokenService.save(enterpriseTokenPo);*//*
*/
/*

                    //TODO 跳转绑定手机号界面
                }*//*

            }

            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},authorize Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }


    @Transactional
    private Response authorizeTencent(String code,String uri,BaseModel userModel) {
        if(StringUtils.isBlank(code)){
            logger.error("authorize Exception：requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.CODE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.CODE.toString()))).build();
        }
        if(StringUtils.isBlank(uri)){
            logger.error("authorize Exception：requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REDIRECT_URI.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REDIRECT_URI.toString()))).build();
        }
        if(StringUtils.isBlank(userModel.getEquipmentType())){
            logger.error("authorize Exception：requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.EQUIPMENT_TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.EQUIPMENT_TYPE.toString()))).build();
        }

        logger.info(" oauth appid:"+qqAppid+"  appsecret:"+qqAppsecret+"  code:"+code+"  uri:"+uri);

        try {
            EnterprisePo enterprisePo=enterpriseService.findByEnterpriseType(SourceEnum.EnterpriseType.WeChat);
            if(enterprisePo==null){
                enterprisePo=new EnterprisePo();
                enterprisePo.setAppId(qqAppid);
                enterprisePo.setAppKey(qqAppsecret);
                enterprisePo.setEnterpriseType(SourceEnum.EnterpriseType.Tencent);
                enterprisePo.setType(EnterpriseEnum.Type.B);
                enterprisePo=  enterpriseService.save(enterprisePo);
            }
            BaseResponse baseResponse=new BaseResponse(requestId);
            SNSUserResponse snsUserResponse=new SNSUserResponse();

            // 用户同意授权
            if (!"authdeny".equals(code)) {
                // 获取网页授权access_token
                Oauth2Token oauth2Token = QQAdvancedUtil.getOauth2AccessToken(qqAppid, qqAppsecret, code, uri, userModel.getEquipmentType());
                // 网页授权接口访问凭证 此ak与普通调用接口使用的ak不同 ,详见:http://mp.enterprise.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
                String accessToken = oauth2Token.getAccessToken();
                // 用户标识
                String openId = oauth2Token.getOpenId();
                // 获取用户信息
                SNSUserInfo snsUserInfo = QQAdvancedUtil.getSNSUserInfo(accessToken, openId, qqAppid, userModel.getEquipmentType());

                if(snsUserInfo != null){
                    snsUserInfo.setScope(oauth2Token.getScope());
                    snsUserResponse.setSnsUserInfo(snsUserInfo);
                    baseResponse.setResult(snsUserResponse);
                    //TODO 授权成功后，做对应业务操纵后返回页面

                    EnterpriseUserPo enterpriseUserPo=new EnterpriseUserPo();
                    enterpriseUserPo.setEnterpriseId(enterprisePo.getId());
                    enterpriseUserPo.setOpenId(snsUserInfo.getThridOpenId());
                    enterpriseUserService.save(enterpriseUserPo);

                    //记录刷新令牌
          */
/*          EnterpriseTokenPo enterpriseTokenPo=new EnterpriseTokenPo();
                    enterpriseTokenPo.setOpenId(snsUserInfo.getOpenId());
                    enterpriseTokenPo.setExpires(oauth2Token.getExpiresIn().toString());
                    enterpriseTokenPo.setRefreshToken(oauth2Token.getRefreshToken());
                    enterpriseTokenPo.setToken(oauth2Token.getAccessToken());
                    enterpriseTokenService.save(enterpriseTokenPo);*//*


                    //TODO 跳转绑定手机号界面
                }
            }

            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},authorize Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
*/

}
