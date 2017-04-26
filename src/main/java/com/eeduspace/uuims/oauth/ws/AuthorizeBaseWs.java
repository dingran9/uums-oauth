package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.oauth.util.CommonUtil;
import com.eeduspace.uuims.oauth.util.UIDGenerator;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Author: dingran
 * Date: 2015/11/9
 * Description:
 */
@Component("auth.BaseWs")
public abstract class AuthorizeBaseWs {

    private final Logger logger = LoggerFactory.getLogger(EnterpriseBaseWs.class);

    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private UserLogService userLogService;

    protected String requestId;

    private Gson gson = new Gson();

    /**
     *
     * @param requestId
     * @param timestamp
     * @param bodyMD5
     * @param accessKey
     * @param action
     * @param code
     * @param redirectUri
     * @param state
     * @param requestBody
     * @param request
     * @return
     */

    @POST
    public Response process(
            @QueryParam("requestId") String requestId,
            @QueryParam("timestamp") String timestamp,
            @QueryParam("bodyMD5") String bodyMD5,
            @QueryParam("accessKey") String accessKey,
            @QueryParam("action") String action,
            @QueryParam("code") String code,
            @QueryParam("redirectUri") String redirectUri,
            @QueryParam("state") String state,
            String requestBody,
            @Context HttpServletRequest request) {

        //进行基本的验证
        /**
         * 1.请求参数的非空验证
         * 2.先判断AccessKeyId，当AccessKeyId以”VE”开头时，则为IDC用户，以”CU”开头则为IDC客户，反之则认证失败；
         * 3.验证token  管理员无需验证token
         * 4.验证timestamp
         * 5.验证bodyMD5
         * 6.验证accesskey
         * 7.验证signature
         * 8.验证权限
         */
        this.requestId = requestId;
        if (StringUtils.isBlank(this.requestId)) {
            this.requestId = UIDGenerator.getUUID();
        }
        logger.info("requestId：{},timestamp：{},bodyMD5：{},code:{},accessKey:{},action:{},redirectUri:{},state:{},requestBody:{}"
                , this.requestId, timestamp, bodyMD5, code, accessKey, action, redirectUri,state,requestBody);

        logger.info("HttpServletRequest: remoteAddr:{},ContextPath:{},RequestURI:{}", CommonUtil.getIpAddress(request),request.getContextPath(),request.getRequestURI());

        //过滤登录
        //第一步

        if (StringUtils.isBlank(action)) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.ACTION.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ACTION.toString()))).build();
        }
        if (StringUtils.isBlank(accessKey)) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.ACCESS_KEY.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.ACCESS_KEY.toString()))).build();
        }
        if (StringUtils.isBlank(bodyMD5)) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.BODY_MD5.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.BODY_MD5.toString()))).build();
        }
        if (StringUtils.isBlank(timestamp)) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.TIMESTAMP.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.TIMESTAMP.toString()))).build();
        }
        if(StringUtils.isBlank(code)){
            logger.error("authorize Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.CODE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.CODE.toString()))).build();
        }
        if(StringUtils.isBlank(redirectUri)){
            logger.error("authorize Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.REDIRECT_URI.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REDIRECT_URI.toString()))).build();
        }
        //第二步
        ManagerPo managerPo = null;
        try {

            //第四步

            long timestampDate = Long.parseLong(timestamp.trim());
            long currentDate = DateUtils.nowTimeMillis();

            long maxDate = currentDate + (1000 * 60 * 10);
            long minDate = currentDate - (1000 * 60 * 10);

            // 验证时间是否过期（超过请求时间戳前后半小时的请求为非法请求）
            if (timestampDate > maxDate || timestampDate < minDate) {
                logger.error("failed to access, timestamp expired.requestId：{}, timestamp:{}", this.requestId, timestamp);
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.TIMESTAMP.toString()))).build();
            }

            //第五步
            //验证请求body是否更改
            if (!Digest.md5Digest(requestBody).equals(bodyMD5)) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString()+"."+ParamName.BODY_MD5.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString(), ParamName.BODY_MD5.toString()))).build();
            }

            //第六步

            managerPo = managerService.findByAccessKeyId(accessKey);
            if (managerPo == null) {
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
            }
            if (UserEnum.Status.IsDelete.equals(managerPo.getStatus()) || UserEnum.Status.Disable.equals(managerPo.getStatus())) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }

        } catch (Exception e) {
            logger.error("The data signature verification error:", e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString()))).build();
        }

        //TODO 验证权限
        return dispatch(action,code,redirectUri,state, requestBody, request, managerPo);

    }

    public abstract Response dispatch(String action,String code,String redirectUri,String state, String requestBody, HttpServletRequest httpServletRequest,
                                      ManagerPo managerPo);
}
