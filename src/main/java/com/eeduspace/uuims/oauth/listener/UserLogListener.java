package com.eeduspace.uuims.oauth.listener;

import com.eeduspace.uuims.oauth.persist.po.UserLogPo;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import javax.inject.Inject;

/**
 * Author: dingran
 * Date: 2016/3/29
 * Description:
 */
public class UserLogListener implements SmartApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(UserLogListener.class);
    private Gson gson=new Gson();

    @Inject
    private UserLogService userLogService;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return false;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return sourceType == UserLogPo.class;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        logger.info("execute create user logs notify recive.");
        UserLogPo userLogPo = (UserLogPo)event.getSource();
        try {
           // logger.debug("execute create user logs notify recive. request:{}",gson.toJson(userLogPo));
            userLogService.save(userLogPo);
        }catch (Exception e){
            logger.error("execute create user logs requestId:{},exception:{}", userLogPo.getRequestId(), e);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
