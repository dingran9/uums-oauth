package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.UserBandModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum.EnterpriseType;
import com.eeduspace.uuims.oauth.persist.po.*;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.UserBandResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.oauth.util.QQAdvancedUtil;
import com.eeduspace.uuims.oauth.util.WXAdvancedUtil;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.eeduspace.uuims.thirdparty.model.Oauth2Token;
import com.eeduspace.uuims.thirdparty.model.SNSUserInfo;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/11/9
 * Description:
 */
@Component
@Path(value = "/band")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class BandWs extends EnterpriseBaseWs{

    private final Logger logger = LoggerFactory.getLogger(BandWs.class);

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
    private UserLogService userLogService;
    @Inject
    private EnterpriseUserService enterpriseUserService;
    @Inject
    private EnterpriseService enterpriseService;
    @Value("${oauth.token.expires}")
    private String expires;
    @Value("${uuims.qq.appid}")
    private String qqAppid;
    @Value("${uuims.qq.appsecret}")
    private String qqAppsecret;
//    @Value("${uuims.qq.bind_qq_redirect_url}")
//    private String bindQQRedirectUrl;
    
    @Value("${uuims.wx.appid}")
    private String wxAppId;
    @Value("${uuims.wx.appsecret}")
    private String wxAppSecret;
    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo) {
        UserModel userModel = gson.fromJson(requestBody, UserModel.class);

        switch (ActionName.toEnum(action)) {
            case BIND_EMAIL:
                return bindEmail(userModel);
            case BIND_TENCENT:
                return bindTencent(userModel);
            case BIND_WECHAT:
                return bindWechat(userModel);
            case BIND_SINA:
                return bindSina(userModel);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 绑定邮箱或者第三方企业
     * @return
     */
    private Response bindEmail(UserModel userModel ) {
        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},bind Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 绑定QQ
     * @return
     */
    private Response bindTencent(UserModel userModel ) {
        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            //todo 认证成功
            if(StringUtils.isBlank(userModel.getCode())){
                logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "CODE");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "CODE"))).build();
            }
            if(StringUtils.isBlank(userModel.getOpenId())){
                logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "OPENID");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "OPENID"))).build();
            }
            if(StringUtils.isBlank(userModel.getCallBackUrl())){
                logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "CALLBACKURL");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "CALLBACKURL"))).build();
            }
            EnterprisePo enterprisePo= enterpriseService.findByEnterpriseType(EnterpriseType.Tencent);
            if(enterprisePo==null){
            	//第三方不存在
            	 logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString());
                 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.ENTERPRISE.toString()))).build();
            }
            UserPo userPo=userService.findByUuid(userModel.getOpenId());
            UserInfoPo userInfoPo=userService.findInfoByUserId(userPo.getId());
            if(userInfoPo==null){
            	//用户详细信息不存在
            	 logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString());
                 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.USER.toString()))).build();
            }
            if (userInfoPo.isBandQQ()) {
            	//已绑定QQ
            	return Response.ok(gson.toJson(baseResponse)).build();
			}
            //UserBandPo userBandPo=new UserBandPo();
            SNSUserInfo snsUserInfo=new SNSUserInfo();
            UserBandModel userBandModel=new UserBandModel();
            try {
         	   snsUserInfo=saveBindRelationship(userInfoPo,enterprisePo, userModel, userPo,EnterpriseType.Tencent);	
 			} catch (Exception e) {
 				 logger.error("requestId：{},bind Exception：", requestId, e);
 		         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
 			}
            userBandModel.setCity(snsUserInfo.getCity());
            userBandModel.setCountry(snsUserInfo.getCountry());
            userBandModel.setHeadImgUrl(snsUserInfo.getHeadImgUrl());
            userBandModel.setNickName(snsUserInfo.getNickname());
            userBandModel.setProvince(snsUserInfo.getProvince());
            userBandModel.setSex(snsUserInfo.getSex()+"");
            userBandModel.setThirdOpenId(snsUserInfo.getThridOpenId());
            userBandModel.setScope(snsUserInfo.getScope());
            UserBandResponse userBandResponse=new UserBandResponse();
            userBandResponse.setUserBandModel(userBandModel);
            baseResponse.setResult(userBandResponse);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},bind Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 绑定微信
     * @return
     */
    private Response bindWechat(UserModel userModel) {
        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            //todo 认证成功
            if(StringUtils.isBlank(userModel.getCode())){
                logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "CODE");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "CODE"))).build();
            }
            if(StringUtils.isBlank(userModel.getOpenId())){
                logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "OPENID");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "OPENID"))).build();
            }
            if(StringUtils.isBlank(userModel.getCallBackUrl())){
                logger.error("bindTencent ExceptionrequestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ "CALLBACKURL");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "CALLBACKURL"))).build();
            }
            EnterprisePo enterprisePo= enterpriseService.findByEnterpriseType(EnterpriseType.WeChat);
            if(enterprisePo==null){
            	//第三方不存在
            	 logger.error("bindWechat ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString());
                 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.ENTERPRISE.toString()))).build();
            }
            UserPo userPo=userService.findByUuid(userModel.getOpenId());
            UserInfoPo userInfoPo=userService.findInfoByUserId(userPo.getId());
            if(userInfoPo==null){
            	//用户详细信息不存在
            	 logger.error("bindWechat ExceptionrequestId："+"requestId,"+ResponseCode.RESOURCE_NOTFOUND.toString());
                 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.USER.toString()))).build();
            }
            if (userInfoPo.isBandWX()) {
            	//已绑定微信
            	return Response.ok(gson.toJson(baseResponse)).build();
			}
            SNSUserInfo snsUserInfo=new SNSUserInfo();
            UserBandModel userBandModel=new UserBandModel();
            try {
            	snsUserInfo=saveBindRelationship(userInfoPo,enterprisePo, userModel, userPo,EnterpriseType.WeChat);	
 			} catch (Exception e) {
 				 logger.error("requestId：{},bind Exception：", requestId, e);
 		         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
 			}
            userBandModel.setCity(snsUserInfo.getCity());
            userBandModel.setCountry(snsUserInfo.getCountry());
            userBandModel.setHeadImgUrl(snsUserInfo.getHeadImgUrl());
            userBandModel.setNickName(snsUserInfo.getNickname());
            userBandModel.setProvince(snsUserInfo.getProvince());
            userBandModel.setSex(snsUserInfo.getSex()+"");
            userBandModel.setThirdOpenId(snsUserInfo.getThridOpenId());
            userBandModel.setScope(snsUserInfo.getScope());
            UserBandResponse userBandResponse=new UserBandResponse();
            userBandResponse.setUserBandModel(userBandModel);
            baseResponse.setResult(userBandResponse);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},bind Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 绑定新浪
     * @return
     */
    private Response bindSina(UserModel userModel) {
        try {
            BaseResponse baseResponse=new BaseResponse(requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},bind Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
    
    /**
     * 保存绑定关系信息（QQ 微信） 
     * Author： zhuchaowei
     * e-mail:zhuchaowei@e-eduspace.com
     * 2016年3月11日 下午3:24:39
     * @param enterprisePo  第三方实体
     * @param userModel  用户模型
     * @param userPo 用户实体
     * @param type 绑定类型 微信 QQ
     */
    protected SNSUserInfo  saveBindRelationship(UserInfoPo userInfoPo,EnterprisePo enterprisePo,UserModel userModel,UserPo userPo,EnterpriseType type) {
    	 SNSUserInfo snsUserInfo=new SNSUserInfo();
    	if(!"authdeny".equals(userModel.getCode())){
			if (type.equals(EnterpriseType.Tencent)) {
				// 网页授权获取token
				Oauth2Token oauth2Token = QQAdvancedUtil.getOauth2AccessToken(
						qqAppid, qqAppsecret, userModel.getCode(),
						userModel.getCallBackUrl(), "web");
				// 网页授权接口访问凭证 此ak与普通调用接口使用的ak不同
				// ,详见:http://mp.enterprise.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
				String accessToken = oauth2Token.getAccessToken();
				// 用户标识
				String openId = oauth2Token.getOpenId();
				// 获取用户信息
				snsUserInfo = QQAdvancedUtil.getSNSUserInfo(accessToken,
						openId, qqAppid, "web");
			}
			if (type.equals(EnterpriseType.WeChat)) {
				try {
					Oauth2Token oauth2Token = WXAdvancedUtil
							.getOauth2AccessToken(wxAppId, wxAppSecret,
									userModel.getCode());
					// 网页授权接口访问凭证 此ak与普通调用接口使用的ak不同
					// ,详见:http://mp.enterprise.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
					String accessToken = oauth2Token.getAccessToken();
					// 用户标识
					String openId = oauth2Token.getOpenId();
					// 获取用户信息
					snsUserInfo = WXAdvancedUtil.getSNSUserInfo(accessToken,
							openId);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    		
    	}
          EnterpriseUserPo enterpriseUserPo=new EnterpriseUserPo();

          //保存用户与第三方关系信息
          enterpriseUserPo.setSex(authConverter.converterSex(snsUserInfo.getSex()));
          enterpriseUserPo.setCreateDate(new Date());
          enterpriseUserPo.setEnterpriseId(enterprisePo.getId());
          enterpriseUserPo.setHeadImgUrl(snsUserInfo.getHeadImgUrl());
          enterpriseUserPo.setNickName(snsUserInfo.getNickname());
          enterpriseUserPo.setThirdOpenId(snsUserInfo.getThridOpenId());
          enterpriseUserPo.setProductId(userPo.getRegisterProductId());
          enterpriseUserPo.setProvince(snsUserInfo.getProvince());
          enterpriseUserPo.setCity(snsUserInfo.getCity());
          enterpriseUserPo.setUserId(userPo.getId());
          enterpriseUserPo.setCountry(snsUserInfo.getCountry());
          enterpriseUserPo.setScope(snsUserInfo.getScope());

          userPo.setEmail(userModel.getEmail());
          userPo.setUpdateDate(new Date());
          userInfoPo.setUpdateDate(new Date());
          //获取详细信息 更改绑定状态为已绑定
        String actionString="";
		switch (type) {
		case Sina:
			userInfoPo.setBandSina(true);
			actionString=ActionName.BIND_SINA.toString();
			break;
		case Tencent:
			userInfoPo.setBandQQ(true);
			actionString=ActionName.BIND_TENCENT.toString();
			break;
		case WeChat:
			userInfoPo.setBandWX(true);
			actionString=ActionName.BIND_WECHAT.toString();
			break;
		case Email:
			userInfoPo.setBandEmail(true);
			actionString=ActionName.BIND_EMAIL.toString();
			break;
		}
		userInfoPo.setUserPo(userPo);
		enterpriseUserService.save(enterpriseUserPo);
		userService.save(userPo, userInfoPo);
        userLogService.create(userPo,actionString, actionString,true,null,null,userModel.getEquipmentType(),requestId);
        return snsUserInfo; 
	}

}
