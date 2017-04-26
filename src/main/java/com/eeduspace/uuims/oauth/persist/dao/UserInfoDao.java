package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.po.UserInfoPo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface UserInfoDao  extends CrudRepository<UserInfoPo, Long> {

    @Query("select ui from UserInfoPo ui where ui.userPo.id =?1 ")
    UserInfoPo findInfoByUserId(Long userId);

    @Modifying
    @Query("delete from UserInfoPo ui where ui.userPo.id =?1 ")
    void  deleteByUserId(Long userId);
}
