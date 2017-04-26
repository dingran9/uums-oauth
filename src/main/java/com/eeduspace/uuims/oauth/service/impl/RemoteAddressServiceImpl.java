package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.RemoteAddressDao;
import com.eeduspace.uuims.oauth.persist.po.RemoteAddressPo;
import com.eeduspace.uuims.oauth.service.RemoteAddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2016/4/5
 * Description:
 */
@Service
public class RemoteAddressServiceImpl implements RemoteAddressService {

    private final Logger logger = LoggerFactory.getLogger(RemoteAddressService.class);

    @Inject
    private RemoteAddressDao remoteAddressDao;
    @Override
    public RemoteAddressPo create(RemoteAddressPo remoteAddressPo) {
        return remoteAddressDao.save(remoteAddressPo);
    }

    @Override
    public RemoteAddressPo save(RemoteAddressPo remoteAddressPo) {
        return remoteAddressDao.save(remoteAddressPo);
    }

    @Override
    public void delete(Long id) {
        remoteAddressDao.delete(id);
    }

    @Override
    public void delete(String address) {
        remoteAddressDao.deleteByRemoteAddress(address);
    }

    @Override
    public List<RemoteAddressPo> findAll() {
        return (List<RemoteAddressPo>) remoteAddressDao.findAll();
    }

    @Override
    public RemoteAddressPo findByAddress(String address) {
        return remoteAddressDao.findByRemoteAddress(address);
    }

    @Override
    public RemoteAddressPo findByUuid(String uuid) {
        return remoteAddressDao.findByUuid(uuid);
    }

    @Override
    public RemoteAddressPo findByAddressAndProductId(String address, Long productId) {
        return remoteAddressDao.findByAddressAndProductId(address,productId);
    }
}
