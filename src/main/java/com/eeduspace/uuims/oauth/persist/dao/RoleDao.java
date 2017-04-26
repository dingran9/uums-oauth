package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.po.RolePo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface RoleDao extends CrudRepository<RolePo, Long> {

    RolePo findByUuid(String uuid);

    RolePo findByType(RoleEnum.Type type);

    List<RolePo> findByStatus(RoleEnum.Status status);

    @Query("select r from RolePo r where r.type=?1 and r.status=?2")
    RolePo findByTypeAndStatus(RoleEnum.Type type,RoleEnum.Status status);
}
