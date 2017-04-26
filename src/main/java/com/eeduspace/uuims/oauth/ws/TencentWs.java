package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.BaseModel;
import com.eeduspace.uuims.oauth.persist.enumeration.EnterpriseEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.EnterprisePo;
import com.eeduspace.uuims.oauth.persist.po.EnterpriseUserPo;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.service.EnterpriseService;
import com.eeduspace.uuims.oauth.service.EnterpriseTokenService;
import com.eeduspace.uuims.oauth.service.EnterpriseUserService;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.eeduspace.uuims.oauth.util.QQAdvancedUtil;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.eeduspace.uuims.thirdparty.model.Oauth2Token;
import com.eeduspace.uuims.thirdparty.model.SNSUserInfo;
import com.eeduspace.uuims.thirdparty.response.SNSUserResponse;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/11/23
 * Description:
 */
@Component
@Path(value = "/qq")
//@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class TencentWs extends AuthorizeBaseWs {

    private final Logger logger = LoggerFactory.getLogger(TencentWs.class);
    private String requestId;
    private Gson gson=new Gson();
    @Inject
    private EnterpriseService enterpriseService;
    @Inject
    private EnterpriseUserService enterpriseUserService;
    @Inject
    private EnterpriseTokenService enterpriseTokenService;
    @Inject
    private UserLogService userLogService;
    @Inject
    private AuthConverter authConverter;
    @Value("${uuims.qq.appid}")
    private String qqAppid;
    @Value("${uuims.qq.appsecret}")
    private String qqAppsecret;
    @Value("${uuims.qq.redirect.uri}")
    private String qqRedirectUrl;

/*
    @POST
    public Response process(
            @QueryParam("requestId") String requestId,
            @QueryParam("action") String action,
            @QueryParam("code") String code,
            @QueryParam("redirectUri") String redirectUri,
            @QueryParam("state") String state,
            String requestBody,
            @Context HttpServletRequest request) {

        //进行基本的验证
        */
/**
         * 1.验证timestamp
         * 2.验证bodyMD5
         * 3.验证token
         *//*

        logger.info("HttpServletRequest: remoteAddr:{},ContextPath:{},RequestURI:{}", CommonUtil.getIpAddress(request),request.getContextPath(),request.getRequestURI());

        this.requestId = requestId;
        if (StringUtils.isBlank(this.requestId)) {
            this.requestId = UIDGenerator.getUUID();
        }
        if (StringUtils.isBlank(action)) {
            logger.error("authorize Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.ACTION.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ACTION.toString()))).build();
        }
        BaseModel baseModel = gson.fromJson(requestBody, BaseModel.class);

        //TODO 验证state
        switch (ActionName.toEnum(action)) {
            case AUTHORIZE_TENCENT:
                return authorizeTencent(code,redirectUri,baseModel);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.AUTHORIZE.toString()))).build();
        }
    }
*/

    @Override
    public Response dispatch(String action,String code,String redirectUri,String state, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo) {
        BaseModel baseModel = gson.fromJson(requestBody, BaseModel.class);
        switch (ActionName.toEnum(action)) {
            case AUTHORIZE_TENCENT:
                return authorizeTencent(code, redirectUri, baseModel);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.AUTHORIZE.toString()))).build();
        }
    }

    @Transactional
    private Response authorizeTencent(String code,String uri,BaseModel baseModel) {
        if(StringUtils.isBlank(baseModel.getEquipmentType())){
            logger.error("authorize Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.EQUIPMENT_TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.EQUIPMENT_TYPE.toString()))).build();
        }

        logger.info(" oauth appid:"+qqAppid+"  appsecret:"+qqAppsecret+"  code:"+code+"  uri:"+uri);

        try {
            EnterprisePo enterprisePo=enterpriseService.findByEnterpriseType(SourceEnum.EnterpriseType.Tencent);
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
                Oauth2Token oauth2Token = QQAdvancedUtil.getOauth2AccessToken(qqAppid, qqAppsecret, code, uri, baseModel.getEquipmentType());
                // 网页授权接口访问凭证 此ak与普通调用接口使用的ak不同 ,详见:http://mp.enterprise.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
                String accessToken = oauth2Token.getAccessToken();
                // 用户标识
                String openId = oauth2Token.getOpenId();
                // 获取用户信息
                SNSUserInfo snsUserInfo = QQAdvancedUtil.getSNSUserInfo(accessToken, openId , qqAppid,baseModel.getEquipmentType());

                if(snsUserInfo != null){
                    snsUserInfo.setScope(oauth2Token.getScope());
                    snsUserResponse.setSnsUserInfo(snsUserInfo);
                    baseResponse.setResult(snsUserResponse);
                    //TODO 授权成功后，做对应业务操纵后返回页面

                    EnterpriseUserPo enterpriseUserPo=new EnterpriseUserPo();
                    enterpriseUserPo.setEnterpriseId(enterprisePo.getId());
                    enterpriseUserPo.setThirdOpenId(snsUserInfo.getThridOpenId());
                    enterpriseUserPo.setNickName(snsUserInfo.getNickname());
                    enterpriseUserPo.setHeadImgUrl(snsUserInfo.getHeadImgUrl());
                    enterpriseUserPo.setLastLoginTime(new Date());
//                    enterpriseUserPo.setProductId(userPo.getRegisterProductId());
                    enterpriseUserPo.setProvince(snsUserInfo.getProvince());
                    enterpriseUserPo.setCity(snsUserInfo.getCity());
                    enterpriseUserPo.setCountry(snsUserInfo.getCountry());
                    enterpriseUserPo.setScope(snsUserInfo.getScope());
                    enterpriseUserService.save(enterpriseUserPo);


                    //TODO 跳转绑定手机号界面
                }
            }
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},authorize Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }



}
