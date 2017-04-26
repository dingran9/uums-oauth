package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.po.EnterpriseTokenPo;
import org.springframework.data.repository.CrudRepository;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface EnterpriseTokenDao extends CrudRepository<EnterpriseTokenPo, Long> {
}
