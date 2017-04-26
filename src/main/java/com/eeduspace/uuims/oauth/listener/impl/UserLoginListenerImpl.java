package com.eeduspace.uuims.oauth.listener.impl;

import com.eeduspace.uuims.oauth.event.UserLoginEvent;
import com.eeduspace.uuims.oauth.listener.UserLoginListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

/**
 * Author: dingran
 * Date: 2016/3/29
 * Description:
 */
@Component
public class UserLoginListenerImpl extends UserLoginListener {

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType == UserLoginEvent.class;
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
