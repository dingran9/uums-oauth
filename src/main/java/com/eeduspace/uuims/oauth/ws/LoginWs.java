package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.ValidateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.BaseModel;
import com.eeduspace.uuims.oauth.model.ManagerModel;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.TokenPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.ManagerResponse;
import com.eeduspace.uuims.oauth.response.UserResponse;
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
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:登录管理
 * UpdateTime 修改时间 03-11 songwei 修改 
 */
@Component
@Path(value = "/login")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class LoginWs {

    private final Logger logger = LoggerFactory.getLogger(LoginWs.class);
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
    @Inject
    private CookieWriteService cookieWriteService;
    @Inject
    private TokenValidateService tokenValidateService;
    @Inject
    private ProductService productService;
    @Inject
    private EventOperationService eventOperationService;
    @Value("${oauth.token.expires}")
    private String expires;
    @Value("${oauth.cookie.expires}")
    private String cookieExpires;
    @Value("${oauth.manager.privateKey}")
    private String privateKey;
    @Value("${online.pidKey}")
	private String onlinePidKey;
	@Value("${online.uidKey}")
	private String onlineUidKey;
	@Value("${online.sidKey}")
	private String onlineSidKey;
    
    @POST
    public Response process(
            @QueryParam("requestId") String requestId,
            @QueryParam("timestamp") String timestamp,
            @QueryParam("bodyMD5") String bodyMD5,
            @QueryParam("token") String token,
            @QueryParam("action") String action,
//            @QueryParam("sessionId") String sessionId,
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

//        logger.debug("timestampDate:{},{}", DateUtils.toString(new Date(timestampDate), DateUtils.DATE_FORMAT_DATETIME), timestampDate);
//        logger.debug("currentDate:{},{}", DateUtils.toString(new Date(currentDate), DateUtils.DATE_FORMAT_DATETIME), currentDate);
//        logger.debug("maxDate:{},{}", DateUtils.toString(new Date(maxDate), DateUtils.DATE_FORMAT_DATETIME), maxDate);
//        logger.debug("minDate:{},{}", DateUtils.toString(new Date(minDate), DateUtils.DATE_FORMAT_DATETIME), minDate);

        // 验证时间是否过期（超过请求时间戳前后半小时的请求为非法请求）
        if (timestampDate > maxDate || timestampDate < minDate) {
            logger.error("failed to access, timestamp expired.requestId：{}, timestamp:{}", this.requestId, timestamp);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.PARAMETER_INVALID.toString(),ParamName.TIMESTAMP.toString()))).build();
        }
        //验证请求body是否更改
        if (!Digest.md5Digest(requestBody).equals(bodyMD5)) {
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_SIGNATURE_DOESNOT_MATCH.toString(),ParamName.BODY_MD5.toString()))).build();
        }

        BaseModel baseModel = gson.fromJson(requestBody, BaseModel.class);

        switch (ActionName.toEnum(action)) {
            case LOGIN:
                return login(baseModel,request,response);
            case MANAGER_LOGIN:
                return managerLogin(baseModel);
            case LDAP:
                return ldap(baseModel,request);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.USER.toString()))).build();
        }
    }

    /**
     * 用户登录
     * @param baseModel 用户登录模型
     * @return
     */
    @Transactional
    private Response login(BaseModel baseModel,HttpServletRequest request,HttpServletResponse response) {

        //TODO 需要传递产品 及 应用设备（WEB/Android等）

        if (StringUtils.isBlank(baseModel.getPassword())) {
            logger.error("User login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(baseModel.getPhone())) {
            logger.error("User login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
        // 验证数据格式
        if(!ValidateUtils.isMobile(baseModel.getPhone())){
            logger.error("User login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ParamName.PHONE.toString()))).build();
        }
        if(StringUtils.isBlank(baseModel.getProductType())){
            logger.error("User login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PRODUCT_TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_TYPE.toString()))).build();
        }
        if(StringUtils.isBlank(baseModel.getEquipmentType())){
            logger.error("User login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.EQUIPMENT_TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.EQUIPMENT_TYPE.toString()))).build();
        }
        try {
            UserPo userPo=userService.findByPhone(baseModel.getPhone());
            if(userPo==null){
                logger.error("User login Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.USER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.USER.toString()))).build();
            }
            if(!UserEnum.Status.Enable.equals(userPo.getStatus())){
                logger.error("User login Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            if(!userPo.getPassword().equals(baseModel.getPassword())){
                logger.debug("userPo.getPassword()="+userPo.getPassword());
                logger.debug("baseModel.getPassword()="+baseModel.getPassword());
                logger.error("User login Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_AUTHFAILURE.toString()))).build();
            }
            SourceEnum.EquipmentType equipmentType= authConverter.converterSourceEquipmentType(baseModel.getEquipmentType());
            if(equipmentType==null){
                logger.error("User login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ParamName.EQUIPMENT_TYPE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.PARAMETER_INVALID.toString(),ParamName.EQUIPMENT_TYPE.toString()))).build();
            }
            ProductPo productPo=productService.findByType(baseModel.getProductType());
            if(productPo==null){
                logger.error("User login Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.PRODUCT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.PRODUCT.toString()))).build();
            }
            BaseResponse baseResponse =new BaseResponse(requestId);
            UserResponse userResponse =new UserResponse();
            UserModel model=authConverter.fromUserPo(userPo,true,false);
            // 当产品禁止用户同设备同时登录时 则删除原登录产生的token
            logger.debug("------------------------------>productPo.getIsManyEquipmentLogin()---->"+productPo.getIsManyEquipmentLogin());
            if(!productPo.getIsManyEquipmentLogin()){
                List<TokenPo> tokenPos = tokenService.findByOpenIdAndPIdAndEquipmentType(userPo.getUuid(), productPo.getId(), equipmentType);
                for(TokenPo po:tokenPos){
                    logger.debug("--待删除 token--->"+po.getToken());
                    redisClientTemplate.del(po.getToken());
                    tokenService.delete(po);
                }
            }
            //TODO 验证是否存在cookie
            //   token设置  设置刷新token  设置时效  设置loginCookie
//            TokenModel tokenModel=new TokenModel() ;
            String token="TK"+ Digest.md5Digest16(baseModel.getPhone() +  DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            String refreshToken= Digest.md5Digest(baseModel.getPhone() + token);
//            int expires_=Integer.parseInt(expires);
//            String loginCookie=Digest.md5Digest(baseModel.getPhone() +DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)+ token);
            String onlineCookie = MessageFormat.format(this.onlineSidKey, model.getOpenId(), RandomUtils.getRandom(6));
            // 将该用户,用户登录的设备类型放入缓存中
//            String onlineUser = MessageFormat.format(this.onlineUidKey, model.getOpenId());
//            String onlineProduct = MessageFormat.format(this.onlinePidKey, String.valueOf(productPo.getId()),baseModel.getEquipmentType(),model.getOpenId());

            model.setToken(token);
            model.setRefreshToken(refreshToken);
            model.setOpenId(userPo.getUuid());
            model.setExpires(expires);
            model.setSessionId(onlineCookie);
            model.setPhone(userPo.getPhone());
            model.setProductId(String.valueOf(productPo.getId()));
            model.setEquipmentType(equipmentType.toString());
 /*
            tokenModel.setToken(token);
            tokenModel.setRefreshToken(refreshToken);
            tokenModel.setOpenId(userPo.getUuid());
            tokenModel.setExpires(expires);
            tokenModel.setSessionId(onlineCookie);
            tokenModel.setEquipmentType(baseModel.getEquipmentType());
            tokenModel.setProductType(baseModel.getProductType());

            redisClientTemplate.setex(token, expires_, gson.toJson(tokenModel));
//            redisClientTemplate.setex(loginCookie, Integer.parseInt(cookieExpires),gson.toJson(tokenModel));
            redisClientTemplate.setex(onlineCookie, Integer.parseInt(cookieExpires), token);
            //将用户信息，用户设备存入缓存
            redisClientTemplate.setex(onlineUser, Integer.parseInt(cookieExpires), gson.toJson(model));
            redisClientTemplate.setex(onlineProduct, Integer.parseInt(cookieExpires), onlineUser);

            
            TokenPo tokenPo=new TokenPo();
            tokenPo.setToken(token);
            tokenPo.setOpenId(userPo.getUuid());
            tokenPo.setRefreshToken(refreshToken);
            tokenPo.setExpires(expires);
            tokenPo.setType(TokenEnum.Type.User);
            tokenPo.setEquipmentType(equipmentType);
            tokenPo.setProductId(productPo.getId());
            tokenService.save(tokenPo,userPo.getUuid());*/

            //调用写入cookie方法（循环写入，都是一样的cookie名字和值）
            cookieWriteService.writeCookies(onlineCookie,response);
/*
            userPo.setLoginStatus(UserEnum.LoginStatus.IsLogin);
            userPo.setUpdateTime(new Date());
            userService.save(userPo);*/

            userResponse.setUserModel(model);
            baseResponse.setResult(userResponse);

            //将用户登录相关信息存入redis
            eventOperationService.userLoginMessage(model);

            userLogService.create(userPo,LogActionEnum.LOGIN.toString(), LogActionEnum.USER.toString(),true,productPo.getId(),null,baseModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
            //TODO 记录日志
        } catch (Exception e) {
            logger.error("requestId：{},User login Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }

    /**
     * 用户登录
     * @param baseModel 管理员登录模型
     * @return
     */
    private Response managerLogin(BaseModel baseModel) {
        if (StringUtils.isBlank(baseModel.getPassword())) {
            logger.error("manager login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PASSWORD.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PASSWORD.toString()))).build();
        }
        if (StringUtils.isBlank(baseModel.getPhone())) {
            logger.error("manager login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PHONE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PHONE.toString()))).build();
        }
/*        // 验证数据格式
        if(!ValidateUtils.isMobile(baseModel.getPhone())){
            logger.error("manager login Exception：requestId："+requestId+","+ResponseCode.PARAMETER_INVALID.toString()+"."+ResourceName.MANAGER.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_INVALID.toString(), ResourceName.MANAGER.toString()))).build();
        }*/

        try {
            //TODO 应用之间  权限的校验 管理员只能登录自己的管理后台
            ManagerPo managerPo=managerService.findByPhone(baseModel.getPhone());
            if(managerPo==null){
                logger.error("manager login Exception：requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.MANAGER.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.RESOURCE_NOTFOUND.toString(),ResourceName.MANAGER.toString()))).build();
            }
            if(!UserEnum.Status.Enable.equals(managerPo.getStatus())){
                logger.error("manager login Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_LOCKED.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            if(!managerPo.getPassword().equals(baseModel.getPassword())){
                logger.error("manager login Exception：requestId："+requestId+","+ResponseCode.FORBIDDEN_AUTHFAILURE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_AUTHFAILURE.toString()))).build();
            }

            BaseResponse baseResponse =new BaseResponse(requestId);
            ManagerResponse managerResponse =new ManagerResponse();
            ManagerModel model=new ManagerModel();
            logger.debug("---->"+baseModel.getPrivateKey());
            logger.debug("---->"+privateKey);
            logger.debug("---->"+StringUtils.isNotBlank(baseModel.getPrivateKey()));
            logger.debug("---->"+baseModel.getPrivateKey().equals(privateKey));
            if(StringUtils.isNotBlank(baseModel.getPrivateKey()) && baseModel.getPrivateKey().equals(privateKey)){
                model =authConverter.fromManagerPo(managerPo,true);
            }else {
                model=authConverter.fromManagerPo(managerPo,false);
            }

            //   token设置  设置刷新token  设置时效  设置loginCookie
            String token="TK"+ Digest.md5Digest16(baseModel.getPhone() +  DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            String refreshToken= Digest.md5Digest(baseModel.getPhone() + token);

            String loginCookie=Digest.md5Digest(baseModel.getPhone() + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + token);

            model.setToken(token);
            model.setExpires(expires);
            model.setRefreshToken(refreshToken);
            model.setSessionId(loginCookie);
            model.setOpenId(managerPo.getUuid());
            model.setPassword(managerPo.getPassword());

/*
            int expires_=Integer.parseInt(expires);
            TokenModel tokenModel=new TokenModel();
            tokenModel.setToken(token);
            tokenModel.setRefreshToken(refreshToken);
            tokenModel.setOpenId(managerPo.getUuid());
            tokenModel.setLoginCookie(loginCookie);
            tokenModel.setExpires(expires);

            //TODO 管理员 是否需要令牌 及登录cookie

             redisClientTemplate.setex(token, expires_, gson.toJson(tokenModel));
            redisClientTemplate.setex(loginCookie, Integer.parseInt(cookieExpires),gson.toJson(tokenModel));

            Cookie cookie=new Cookie("com.eedcspace.uuims",loginCookie);
            cookie.setMaxAge(3600*24*7);
            response.addCookie(cookie);

            TokenPo tokenPo=new TokenPo();
            tokenPo.setToken(token);
            tokenPo.setOpenId(managerPo.getUuid());
            tokenPo.setRefreshToken(refreshToken);
            tokenPo.setExpires(expires);
            tokenPo.setType(TokenEnum.Type.Manager);
            tokenService.save(tokenPo);*/

//            redisClientTemplate.setex(token, expires_, gson.toJson(tokenModel));

            managerResponse.setManagerModel(model);
            baseResponse.setResult(managerResponse);
            //TODO 记录日志
            managerLogService.create(managerPo,null, LogActionEnum.MANAGER_LOGIN.toString(), LogActionEnum.MANAGER.toString(),true,managerPo.getProductPo(),null,baseModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();

        } catch (Exception e) {
            logger.error("requestId：{},manager login Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();
        }

    }

    /**
     * 单点登录
     * @param baseModel
     * @return
     */
    private Response ldap(BaseModel baseModel , HttpServletRequest request) {

        //TODO 需要传递产品ID 及 应用设备（WEB/Android等）
        /**
         * 准备：用户登录后 记录浏览器或app cookie
         * 1.获取cookie 是否存在
         * 2.如果存在cookie 则根据cookie信息生成token 并返回
         * 3.如果不存在 则页面跳转到登录页面
         */

        try {
            String loginCookie="test";
            if(StringUtils.isNotBlank(baseModel.getSessionId())){
                loginCookie=baseModel.getSessionId();
            }else{
                Cookie[] cookie= request.getCookies();
                if(cookie!=null && cookie.length>0){
                    for(Cookie c:cookie){
                        logger.debug("-------------------------c.getName()-->"+c.getName());
                        logger.debug("-------------------------c.getValue()-->"+c.getValue());
                        if(c.getName().equals("eedcspace.uuims.cookie")){
                            loginCookie=c.getValue();
                        }
                    }
                }
            }
            logger.debug("-----------loginCookie--->"+loginCookie);
            String cookie_= redisClientTemplate.get(loginCookie);
            if(StringUtils.isBlank(cookie_)){
                logger.error("requestId："+requestId+",ldap Exception：cookie is null");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.NOT_LOGIN.toString()))).build();
            }
            TokenModel tokenModel = gson.fromJson(cookie_, TokenModel.class);
            TokenModel tokenModel2 = tokenValidateService.tokenValidate(requestId,tokenModel.getToken(),tokenModel);
            if(tokenModel2==null || StringUtils.isBlank(tokenModel2.getOpenId())){
            	  logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_TOKENFAILURE.toString());
                  return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_TOKENFAILURE.toString()))).build();
            }
          /*  UserModel userModel= gson.fromJson(cookie_,UserModel.class);
            String openId=userModel.getOpenId();
            if(StringUtils.isBlank(openId)){
                redisClientTemplate.del(loginCookie);
                logger.error("requestId："+requestId+",ldap Exception：UserModel.openId is null");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.NOT_LOGIN.toString()))).build();
            }*/
            UserPo userPo=userService.findByUuid(tokenModel2.getOpenId());
            if(userPo==null){
                logger.error("requestId："+requestId+",ldap Exception：userPo is null");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.NOT_LOGIN.toString()))).build();
            }
            //验证用户状态
            if(!UserEnum.Status.Enable.equals(userPo.getStatus())){
                logger.error("requestId："+requestId+",ldap Exception：userPo is FORBIDDEN_LOCKED");
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId),ResponseCode.FORBIDDEN_LOCKED.toString()))).build();
            }
            //生成token
           /* String token="TK"+ Digest.md5Digest16(userPo.getPhone() +  DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
            String refreshToken= Digest.md5Digest(userPo.getPhone() + token);
            int expires_=Integer.parseInt(expires);*/
            //重新刷新缓存中cookie的失效时间
            redisClientTemplate.expire(cookie_, Integer.valueOf(cookieExpires));
            
            UserModel model=new UserModel();
            model.setToken(tokenModel2.getToken());
            model.setRefreshToken(tokenModel2.getRefreshToken());
            model.setExpires(expires);
            model.setOpenId(tokenModel2.getOpenId());
            model.setSessionId(loginCookie);
            model.setPhone(userPo.getPhone());

/* 			TokenModel tokenModel=new TokenModel();
            tokenModel.setToken(token);
            tokenModel.setRefreshToken(refreshToken);
            tokenModel.setOpenId(userPo.getUuid());
            tokenModel.setExpires(expires);
            tokenModel.setSessionId(loginCookie);
            redisClientTemplate.setex(token, expires_, gson.toJson(tokenModel));*/

/*          //TODO 后期记录用户与产品 登录状态关连
            userPo.setLoginStatus(UserEnum.LoginStatus.IsLogin);
            userPo.setUpdateDate(new Date());
            userService.save(userPo);*/


            BaseResponse baseResponse=new BaseResponse(requestId);
            UserResponse userResponse=new UserResponse();
            userResponse.setUserModel(model);
            baseResponse.setResult(userResponse);
            return Response.ok(gson.toJson(baseResponse)).build();

        }catch (Exception e){
            logger.error("requestId：{},ldap Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.USER.toString()))).build();

        }
    }
    

}
