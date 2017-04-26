package com.eeduspace.uuims.oauth.aop;

import com.eeduspace.uuims.oauth.service.ManagerLogService;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.service.UserLogService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.google.gson.Gson;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Author: dingran
 * Date: 2015/12/2
 * Description:
 */
@Aspect
public class LogAop {

    private final Logger logger = LoggerFactory.getLogger(LogAop.class);
    @Inject
    private Gson gson;
    @Inject
    private UserService userService;
    @Inject
    private UserLogService userLogService;
    @Inject
    private ManagerService managerService;
    @Inject
    private ManagerLogService managerLogService;

    /**
     * 记录 日志
     * @param pjp
     * @return
     * @throws org.springframework.web.HttpSessionRequiredException
     */
  //  @Around("execution(* com.eeduspace.uuims.oauth.ws.*.*(..))")
    public Object doAround(ProceedingJoinPoint pjp) throws Exception {
      return null;
    }
}
