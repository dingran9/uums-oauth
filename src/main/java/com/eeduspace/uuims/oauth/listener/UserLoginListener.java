package com.eeduspace.uuims.oauth.listener;

import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.TokenModel;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.TokenEnum;
import com.eeduspace.uuims.oauth.persist.po.TokenPo;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.service.TokenService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import javax.inject.Inject;

/**
 * Author: dingran
 * Date: 2016/3/29
 * Description:监听用户登录
 */
public class UserLoginListener implements SmartApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginListener.class);
    private Gson gson=new Gson();


    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private TokenService tokenService;
    @Inject
    private AuthConverter authConverter;

    @Value("${oauth.cookie.expires}")
    private String cookieExpires;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return false;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return sourceType == UserModel.class;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        logger.info("execute user login notify recive");
        UserModel userModel = (UserModel)event.getSource();
        try{
            logger.debug("execute user login notify recive. request:{}",gson.toJson(userModel));
            // 将该用户,用户登录的设备类型放入缓存中
            String onlineUser = SourceEnum.OnlineSourceType.OnlineUid.getValue() + userModel.getOpenId();
            String onlineProduct = SourceEnum.OnlineSourceType.OnlinePid.getValue() +userModel.getProductId() + SourceEnum.OnlineSourceType.UnderLine.getValue()
                    + userModel.getEquipmentType() + SourceEnum.OnlineSourceType.UnderLine.getValue() + userModel.getOpenId();
            int expires_=Integer.parseInt(userModel.getExpires());

            //   token设置  设置刷新token  设置时效  设置loginCookie
            TokenModel tokenModel=new TokenModel() ;
            tokenModel.setToken(userModel.getToken());
            tokenModel.setRefreshToken(userModel.getRefreshToken());
            tokenModel.setOpenId(userModel.getOpenId());
            tokenModel.setExpires(userModel.getExpires());
            tokenModel.setSessionId(userModel.getSessionId());
            tokenModel.setEquipmentType(userModel.getEquipmentType());
            tokenModel.setProductType(userModel.getProductType());
            //redis-setex(key,过期时间expires，value)
            redisClientTemplate.setex(userModel.getToken(), expires_, gson.toJson(tokenModel));
//            redisClientTemplate.setex(loginCookie, Integer.parseInt(cookieExpires),gson.toJson(tokenModel));
            redisClientTemplate.setex(userModel.getSessionId(), Integer.parseInt(cookieExpires), userModel.getToken());
            //将用户信息，用户设备存入缓存
            redisClientTemplate.setex(onlineUser, Integer.parseInt(cookieExpires), gson.toJson(userModel));
            redisClientTemplate.setex(onlineProduct, Integer.parseInt(cookieExpires), onlineUser);

            TokenPo tokenPo=new TokenPo();
            tokenPo.setToken(userModel.getToken());
            tokenPo.setOpenId(userModel.getOpenId());
            tokenPo.setRefreshToken(userModel.getRefreshToken());
            tokenPo.setExpires(userModel.getExpires());
            tokenPo.setType(TokenEnum.Type.User);
            tokenPo.setEquipmentType(authConverter.converterSourceEquipmentType(userModel.getEquipmentType()));
            tokenPo.setProductId(Long.parseLong(userModel.getProductId()));
            tokenService.save(tokenPo,userModel.getOpenId());
        }catch (Exception e){
            logger.error("requestId：{},User login Exception：",userModel.getRequestId() , e);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
