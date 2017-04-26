package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.EnterprisePo;
import org.springframework.data.repository.CrudRepository;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface EnterpriseDao  extends CrudRepository<EnterprisePo, Long> {

    EnterprisePo findByEnterpriseType(SourceEnum.EnterpriseType type);
}
