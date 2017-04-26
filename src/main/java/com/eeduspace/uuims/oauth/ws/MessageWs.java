package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.MessageModel;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.RemoteAddressPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.MessageResponse;
import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.service.RemoteAddressService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.oauth.util.CommonUtil;
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

/**
 * Author: dingran
 * Date: 2016/1/20
 * Description:
 */
@Component
@Path(value = "/message")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class MessageWs {
    private final Logger logger = LoggerFactory.getLogger(MessageWs.class);
    private String requestId;
    private Gson gson=new Gson();

    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private AuthConverter authConverter;
    @Inject
    private ManagerLogService managerLogService;
    @Inject
    private RemoteAddressService remoteAddressService;
    @Value("${oauth.token.expires}")
    private String expires;
    @Value("${oauth.cookie.expires}")
    private String cookieExpires;
    @Value("${oauth.manager.privateKey}")
    private String privateKey;
    @POST
    public Response process(
            @QueryParam("requestId") String requestId,
            @QueryParam("timestamp") String timestamp,
            @QueryParam("bodyMD5") String bodyMD5,
            @QueryParam("action") String action,
            @QueryParam("remoteAddr") String remoteAddr,
            String requestBody,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        //TODO  获取平台、终端等信息
        //进行基本的验证
        /**
         * 1.验证timestamp
         * 2.验证bodyMD5
         * 3.验证用户名密码
         */
        this.requestId = requestId;
        if(StringUtils.isBlank(this.requestId)){
            this.requestId  = UIDGenerator.getUUID();
        }
        logger.info("requestId：{},timestamp：{},bodyMD5：{},remoteAddr:{},requestBody:{}"
                ,this.requestId, timestamp, bodyMD5,remoteAddr, requestBody );
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

        MessageModel messageModel = gson.fromJson(requestBody, MessageModel.class);


        if (StringUtils.isBlank(messageModel.getManagerId())) {
            logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.OPENID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.OPENID.toString()))).build();
        }
        if (StringUtils.isBlank(messageModel.getPassword())) {
            logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(messageModel.getRemoteAddress())) {
            logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.REMOTE_ADDRESS.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.REMOTE_ADDRESS.toString()))).build();
        }
        ManagerPo managerPo=null;
        try {
            //TODO 应用之间  权限的校验 管理员只能登录自己的管理后台
            managerPo=managerService.findByUuid(messageModel.getManagerId());
            if(managerPo==null){
                logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.MANAGER.toString()))).build();
            }
            if(!UserEnum.Status.Enable.equals(managerPo.getStatus())){
                logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            if(!managerPo.getPassword().equals(messageModel.getPassword())){
                logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_AUTHFAILURE.toString()))).build();
            }
            if(managerPo.getProductPo()==null){
                logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.PRODUCT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.PRODUCT.toString()))).build();
            }
            logger.debug("manager sendSMS productPo"+managerPo.getProductPo().getType());
            //TODO 验证 IP
//            RemoteAddressPo remoteAddressPo = remoteAddressService.findByAddressAndProductId(messageModel.getRemoteAddress(), managerPo.getProductPo().getId());
            RemoteAddressPo remoteAddressPo = remoteAddressService.findByAddress(messageModel.getRemoteAddress());
            if(remoteAddressPo==null){
                logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString(),ResourceName.REMOTE_ADDRESS.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
        }catch (Exception e){
            logger.error("requestId：{},manager sendSMS Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

        switch (ActionName.toEnum(action)) {
            case SEND_SMS:
                return sendSMS(messageModel,managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 发送消息 验证
     * @param messageModel
     * @return
     */
    private Response sendSMS(MessageModel messageModel,ManagerPo managerPo) {
/*        if (StringUtils.isBlank(messageModel.getUserPhone())) {
            logger.error("manager sendSMS Exception：requestId：" + requestId + "," + ResponseCode.PARAMETER_MISS.toString() + "." + ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        if (StringUtils.isBlank(messageModel.getSendType())) {
            logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.SEND_TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.SEND_TYPE.toString()))).build();
        }*/
        try {
            UserPo userPo=null;
            //TODO 暂不使用
     /*       if(!messageModel.getSendType().equals("register")){
                 userPo= userService.findByPhone(messageModel.getUserPhone());
                 if(userPo==null){
                     logger.error("manager sendSMS Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString(),ParamName.PHONE.toString());
                     return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.USER.toString()))).build();
                 }
            }*/
            BaseResponse baseResponse =new BaseResponse(requestId);
            MessageResponse messageResponse =new MessageResponse();
            MessageModel model=authConverter.fromMessageModel(managerPo,userPo);
            messageResponse.setMessageModel(model);
            baseResponse.setResult(messageResponse);
            //TODO 记录日志
            managerLogService.create(managerPo,null, LogActionEnum.SEND_SMS.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,messageModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},manager sendSMS Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
}
