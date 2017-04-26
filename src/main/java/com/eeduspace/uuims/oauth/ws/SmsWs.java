package com.eeduspace.uuims.oauth.ws;


import java.net.URLDecoder;
import java.util.Date;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.ValidateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.model.SmsModel;
import com.eeduspace.uuims.oauth.persist.po.*;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.oauth.util.CommonUtil;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.eeduspace.uuims.oauth.util.SMSUtil;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;




@Component
@Path(value = "/user/restPwd")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class SmsWs{

    private final Logger logger = LoggerFactory.getLogger(SmsWs.class);

    private Gson gson = new Gson();
    private String requestId;
    @Inject
    private UserService userService;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerService managerService;
    @Inject
    private SMSUtil smsUtil;
    @Value("${oauth.token.expires}")
    private String expires;
    @Value("${user.sendSMS.code.expires}")
    private String sendSMSCodeExpires;
    @Value("${uuims.sms.sendType}")
    private String register;
    @Value("${uuims.sms.sendType.03}")
    private String teacherCertificate;
    @Value("${uuims.sms.sendType.01}")
    private String resetPassword;
    @Value("${uuims.sms.sendType.02}")
    private String editPassword;
    @Value("${uuims.sms.sendType.04}")
    private String comcode;
   
    
    @POST
    public Response process(   
            @QueryParam("requestId") String requestId,
            @QueryParam("timestamp") String timestamp,
            @QueryParam("bodyMD5") String bodyMD5,
            @QueryParam("token") String token,
            @QueryParam("accessKey") String accessKey,
            @QueryParam("action") String action,
            @QueryParam("signature") String signature,
            @QueryParam("key") String key,
            String requestBody,
            @Context HttpServletRequest request) {
    	logger.info("requestId：{},timestamp：{},bodyMD5：{},token:{},action:{},requestBody:{}"
                 ,this.requestId, timestamp, bodyMD5,token,action, requestBody );
        logger.info("HttpServletRequest: remoteAddr:{},ContextPath:{},RequestURI:{}", CommonUtil.getIpAddress(request),request.getContextPath(),request.getRequestURI());
        this.requestId = requestId;
        if (StringUtils.isBlank(this.requestId)) {
            this.requestId = UIDGenerator.getUUID();
        }
        if (StringUtils.isBlank(action)) {
            logger.error("authorize Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ ParamName.ACTION.toString());
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
        if (StringUtils.isBlank(signature)) {
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.SIGNATURE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.SIGNATURE.toString()))).build();
        }
        //第二步
        ManagerPo managerPo = null;
        try {
            String aKType;
            if (accessKey.startsWith("VE")) {
                aKType = "VE";
            }else {
                logger.error("The request parameter is invalid.ACCESSKEY.requestId：{}, timestamp:{}", this.requestId, accessKey);
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.ACCESS_KEY.toString()))).build();
            }
            //第四步
            long timestampDate = Long.parseLong(timestamp.trim());
            long currentDate = DateUtils.nowTimeMillis();
            long maxDate = currentDate + (1000 * 60 * 10);
            long minDate = currentDate - (1000 * 60 * 10);
            logger.debug("timestampDate:{},{}", DateUtils.toString(new Date(timestampDate), DateUtils.DATE_FORMAT_DATETIME), timestampDate);
            logger.debug("currentDate:{},{}", DateUtils.toString(new Date(currentDate), DateUtils.DATE_FORMAT_DATETIME), currentDate);
            logger.debug("maxDate:{},{}", DateUtils.toString(new Date(maxDate), DateUtils.DATE_FORMAT_DATETIME), maxDate);
            logger.debug("minDate:{},{}", DateUtils.toString(new Date(minDate), DateUtils.DATE_FORMAT_DATETIME), minDate);
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
            String secretKey = "";
            if (!"VE".equals(aKType)) {
            	logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+"secretKey");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), "secretKey"))).build();
            }
            managerPo = managerService.findByAccessKeyId(accessKey);
            if (managerPo == null) {
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.MANAGER.toString()))).build();
            }
            if (UserEnum.Status.IsDelete.equals(managerPo.getStatus()) || UserEnum.Status.Disable.equals(managerPo.getStatus())) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            secretKey = managerPo.getSecretKey();
            //第七步
            String _signature = Digest.getSignature((accessKey+"\n"+action+"\n"+timestamp+"\n"+ bodyMD5).getBytes(), secretKey.getBytes());
            if (!URLDecoder.decode(signature, "UTF-8").equals(URLDecoder.decode(_signature, "UTF-8"))) {
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString()+"."+ParamName.SIGNATURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString(), ParamName.SIGNATURE.toString()))).build();
            }
        } catch (Exception e) {
            logger.error("The data signature verification error:", e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString()))).build();
        }
        SmsModel smsModel = gson.fromJson(requestBody, SmsModel.class);

        //TODO 验证state
        switch (ActionName.toEnum(action)) {
        case SEND_SMS://发送重置密码的手机验证码
            return sendSMS(smsModel,managerPo); 
        case VALIDATE_CODE://重置密码
            return validateSMSCode(smsModel);
        case RESET_PASSWORD://重置密码
            return resetPassword(smsModel);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.AUTHORIZE.toString()))).build();
        }
    }
	/**
     * 用户发送短信
     */
	private Response sendSMS(SmsModel smsModel,ManagerPo managerPo) {
        try {
        	BaseResponse baseResponse = new BaseResponse(requestId);
        	if (StringUtils.isBlank(smsModel.getPhone())) {
	             logger.error("sms sendSMS Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
	             return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
	         }
	    	  // 验证数据格式
	        if (!ValidateUtils.isMobile(smsModel.getPhone())) {
	            logger.error("sms sendSMS Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.PHONE.toString());
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
	         }
	        //手机号查实体
            UserPo userPo = userService.findByPhone(smsModel.getPhone());
            if (userPo == null) {
                logger.error("sms sendSMS Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ParamName.PHONE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ParamName.PHONE.toString()))).build();
            }
            //获取手机号的code
	    	String code = smsUtil.send(userPo.getPhone(), resetPassword,managerPo.getPassword(),managerPo.getUuid());
	    	if (StringUtils.isBlank(code)) {
	    	    logger.error("sms sendSMS Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+ParamName.CODE.toString());
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ParamName.CODE.toString()))).build();
	    	}
	    	String ticket = RandomUtils.getRandom(0);
	    	SmsModel model = new SmsModel();
	    	model.setPhone(userPo.getPhone());
	    	model.setStatus(UserEnum.ReviewStatus.UnReview.getValue());
	    	model.setTicket(ticket);
	    	model.setCode(code);
	    	redisClientTemplate.setex("uuims_restpwd_validate_code"+userPo.getUuid(),Integer.parseInt(sendSMSCodeExpires),gson.toJson(model));
	    	SmsModel sms = new SmsModel();
	    	sms.setTicket(ticket);
	    	baseResponse.setResult(sms);
	    	return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("sms sendSMS Exception：requestId：{},activation Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
	 /**
     * 验证验证码
     */
	private Response validateSMSCode(SmsModel smsModel) {
        try {
        	BaseResponse baseResponse = new BaseResponse(requestId);
        	if (StringUtils.isBlank(smsModel.getCode())) {
	             logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.CODE.toString());
	             return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.CODE.toString()))).build();
	         }
        	if (StringUtils.isBlank(smsModel.getTicket())) {
	             logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+"Ticket");
	             return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "Ticket"))).build();
	         }
        	if (StringUtils.isBlank(smsModel.getPhone())) {
	             logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
	             return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
	         }
	    	// 验证数据格式
	        if (!ValidateUtils.isMobile(smsModel.getPhone())) {
	            logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.PHONE.toString());
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
	         }
	        //手机号查实体
            UserPo userPo = userService.findByPhone(smsModel.getPhone());
            if (userPo == null) {
                logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"userPo is null");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),"userPo is null"))).build();
            }
    	    String redisBody = redisClientTemplate.get("uuims_restpwd_validate_code"+userPo.getUuid());
    	    SmsModel fromJson = gson.fromJson(redisBody, SmsModel.class);
    	    if (fromJson==null || StringUtils.isBlank(fromJson.getCode()) || StringUtils.isBlank(fromJson.getTicket())) {
    	    	 logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"sms redis is null");
                 return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),"sms redis is null"))).build();	
			}
    	    if (!smsModel.getTicket().equals(fromJson.getTicket())) {
     		    logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString()+"Ticket");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_AUTHFAILURE.toString(), "Ticket"))).build();
 	     	}
    	    if (!smsModel.getCode().equals(fromJson.getCode())) {
    		   logger.error("sms validateSMSCode  Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString()+ParamName.CODE.toString());
               return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_AUTHFAILURE.toString(), ParamName.CODE.toString()))).build();
	     	}
    	    SmsModel model = new SmsModel();
	    	model.setPhone(userPo.getPhone());
	    	model.setStatus(UserEnum.ReviewStatus.ReviewAdopt.getValue());
	    	model.setTicket(fromJson.getTicket());
	    	redisClientTemplate.setex("uuims_restpwd_validate_code"+userPo.getUuid(),Integer.parseInt(sendSMSCodeExpires),gson.toJson(model));
    	    baseResponse.setResult(model);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("sms validateSMSCode  Exception：requestId：{},activation Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }
    }
	/**
	 * 用户重置密码
	 */
	 private Response resetPassword(SmsModel smsModel) {
		BaseResponse baseResponse = new BaseResponse(requestId);
		try { 
	    	if (StringUtils.isBlank(smsModel.getTicket())) {
	             logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+"Ticket");
	             return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), "Ticket"))).build();
	         }
	    	if (StringUtils.isBlank(smsModel.getPhone())) {
	             logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
	             return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
	         }
	    	if (StringUtils.isBlank(smsModel.getPassword())) {
				 logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
	        }
	    	// 验证数据格式
	        if (!ValidateUtils.isMobile(smsModel.getPhone())) {
	            logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+ParamName.PHONE.toString());
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
	         }
		     //手机号查实体
	        UserPo userPo = userService.findByPhone(smsModel.getPhone());
	        if (userPo == null) {
	            logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"userPo is null");
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),"userPo is null"))).build();
	        }
	 	    String redisBody = redisClientTemplate.get("uuims_restpwd_validate_code"+userPo.getUuid());
	 	    
	 	    SmsModel fromJson = gson.fromJson(redisBody, SmsModel.class);
	 	    
	 	    if (fromJson==null || fromJson.getStatus()==null|| StringUtils.isBlank(fromJson.getTicket())) {
		    	logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"sms redis is null");
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(),"sms redis is null"))).build();	
			}
	 	    if (!smsModel.getTicket().equals(fromJson.getTicket())) {
			    logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString()+"Ticket");
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_AUTHFAILURE.toString(), "Ticket"))).build();
	    	}
	 	    if (fromJson.getStatus()!=UserEnum.ReviewStatus.ReviewAdopt.getValue()) {
			    logger.error("sms resetPassword  Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString()+"Status");
	            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_AUTHFAILURE.toString(), "Status"))).build();
	    	}
	 	   //userPo.setPassword(Digest.md5Digest(smsModel.getPassword()));
	 	   userPo.setPassword(smsModel.getPassword());
           userService.save(userPo);
           userLogService.create(userPo, LogActionEnum.RESET_PASSWORD.toString(), LogActionEnum.USER.toString(),true,null,null,null,requestId);
	       return Response.ok(gson.toJson(baseResponse)).build();
	     } catch (Exception e) {
	         logger.error("resetPassword  Exception：requestId：{},activation Exception：", requestId, e);
	         return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
	     }
	 }
}
