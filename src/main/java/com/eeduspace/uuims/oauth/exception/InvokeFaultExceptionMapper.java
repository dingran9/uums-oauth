package com.eeduspace.uuims.oauth.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

/**
 * Author: dingran
 * Date: 2016/5/6
 * Description:
 */
@Provider
public class InvokeFaultExceptionMapper  implements ExceptionMapper {
    @Override
    public Response toResponse(Throwable ex) {
        StackTraceElement[] trace = new StackTraceElement[1];
        trace[0] = ex.getStackTrace()[0];
        ex.setStackTrace(trace);
        Response.ResponseBuilder rb = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        rb.type("application/json;charset=UTF-8");
        rb.entity(ex);
        rb.language(Locale.SIMPLIFIED_CHINESE);
        Response r = rb.build();
        return r;
    }
}
