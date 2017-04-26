package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.po.RemoteAddressPo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Author: dingran
 * Date: 2016/4/5
 * Description:
 */
public interface RemoteAddressDao extends CrudRepository<RemoteAddressPo, Long> {

    /**
     * 根据 uuid 获取
     * @param uuid
     * @return
     */
    RemoteAddressPo findByUuid(String uuid);

    /**
     * 根据IP 获取
     * @param remoteAddress
     * @return
     */
    RemoteAddressPo findByRemoteAddress(String remoteAddress);
    /**
     * 根据 ip及产品 获取
     * @return
     */
    @Query("select r from RemoteAddressPo r where r.remoteAddress =?1 and r.productPo.id=?2")
    RemoteAddressPo findByAddressAndProductId(String remoteAddress,Long productId);
    @Modifying
    @Query("delete from RemoteAddressPo r where r.remoteAddress =?1 ")
    void  deleteByRemoteAddress(String remoteAddress);
}
