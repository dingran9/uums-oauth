package com.eeduspace.uuims.oauth.listener.impl;

import com.eeduspace.uuims.oauth.event.UserLogEvent;
import com.eeduspace.uuims.oauth.listener.UserLogListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

/**
 * Author: dingran
 * Date: 2016/3/29
 * Description:
 */
@Component
public class UserLogListenerImpl extends UserLogListener{

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType == UserLogEvent.class;
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
