package com.eeduspace.uuims.oauth.event;

import org.springframework.context.ApplicationEvent;

/**
 * Author: dingran
 * Date: 2016/3/29
 * Description:
 */
public class UserLogEvent  extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public UserLogEvent(Object source) {
        super(source);
    }
}
