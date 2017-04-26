package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.RemoteAddressPo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2016/4/5
 * Description:IP白名单 管理
 */
public interface RemoteAddressService {

    /**
     * 新增 IP白名单
     * @param remoteAddressPo
     * @return
     */
    RemoteAddressPo create(RemoteAddressPo remoteAddressPo);
    /**
     * 保存 IP白名单
     * @param remoteAddressPo
     * @return
     */
    RemoteAddressPo save(RemoteAddressPo remoteAddressPo);
    /**
     *删除白名单
     * @param id
     */
    void delete(Long id);

    /**
     *删除白名单
     * @param address
     */
    void delete(String address);

    /**
     *获取所有
     * @return
     */
    List<RemoteAddressPo> findAll();

    /**
     *根据IP 获取
     * @param address
     * @return
     */
    RemoteAddressPo findByAddress(String address);


    /**
     * 根据UUID获取
     * @param uuid
     * @return
     */
    RemoteAddressPo findByUuid(String uuid);

    /**
     *根据IP 获取
     * @param address
     * @return
     */
    RemoteAddressPo findByAddressAndProductId(String address,Long productId);

}
