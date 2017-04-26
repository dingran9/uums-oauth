package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.event.UserLogEvent;
import com.eeduspace.uuims.oauth.event.UserLoginEvent;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.po.UserLogPo;
import com.eeduspace.uuims.oauth.service.EventOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Author: dingran
 * Date: 2016/3/29
 * Description:
 */

@Component
public class EventOperationServiceImpl implements EventOperationService{
    @Autowired
    private ApplicationContext applicationContext;

    @Async
    public void createUserLogMessage(UserLogPo userLogPo) {
        applicationContext.publishEvent(new UserLogEvent(userLogPo));
    }

    @Async
    public void userLoginMessage(UserModel userModel) {
        applicationContext.publishEvent(new UserLoginEvent(userModel));
    }
}
