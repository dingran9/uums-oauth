package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.persist.po.UserLogPo;

/**
 * Author: dingran
 * Date: 2016/3/29
 * Description:
 */
public interface EventOperationService {

    public void createUserLogMessage(UserLogPo userLogPo);

    public void userLoginMessage(UserModel userModel);
}
