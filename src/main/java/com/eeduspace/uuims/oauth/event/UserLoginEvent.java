package com.eeduspace.uuims.oauth.event;

import org.springframework.context.ApplicationEvent;

/**
 * Author: dingran
 * Date: 2016/3/30
 * Description:
 */
public class UserLoginEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public UserLoginEvent(Object source) {
        super(source);
    }
}
