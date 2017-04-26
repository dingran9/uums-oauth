package com.eeduspace.uuims.oauth.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * Author: dingran
 * Date: 2016/3/28
 * Description:
 */
public class HelloInInterceptor extends AbstractPhaseInterceptor<Message> {

    public HelloInInterceptor(String phase) {
        super(phase);
    }

    public HelloInInterceptor() {
        super(Phase.RECEIVE);
    }

    /** <功能详细描述>
     * 创 建 人:  XX
     * 创建时间:  2012-9-28 下午02:34:07
     * @param arg0
     * @throws Fault
     * @see [类、类#方法、类#成员]
     */
    public void handleMessage(Message message) throws Fault {
        System.out.println("*********In****Helloworld******");
    }

}
