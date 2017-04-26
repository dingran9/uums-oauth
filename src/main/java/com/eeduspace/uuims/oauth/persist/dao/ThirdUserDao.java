package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.po.EnterpriseUserPo;
import org.springframework.data.repository.CrudRepository;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface ThirdUserDao  extends CrudRepository<EnterpriseUserPo, Long> {

    EnterpriseUserPo findByUserId(String openId);

    EnterpriseUserPo findByPhone(String phone);
}
