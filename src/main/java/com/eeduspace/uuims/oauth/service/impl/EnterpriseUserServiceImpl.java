package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.ThirdUserDao;
import com.eeduspace.uuims.oauth.persist.po.EnterpriseUserPo;
import com.eeduspace.uuims.oauth.service.EnterpriseUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:第三方用户管理
 */
@Service
public class EnterpriseUserServiceImpl implements EnterpriseUserService {

    private final Logger logger = LoggerFactory.getLogger(EnterpriseUserServiceImpl.class);

    @Inject
    private ThirdUserDao thirdUserDao;

    @Override
    public List<EnterpriseUserPo> findAll() {
        return (List<EnterpriseUserPo>) thirdUserDao.findAll();
    }

    @Override
    public EnterpriseUserPo findOne(Long thirdUserId) {
        return thirdUserDao.findOne(thirdUserId);
    }

    @Override
    public EnterpriseUserPo findByOpenId(String openId) {
        return thirdUserDao.findByUserId(openId);
    }

    @Override
    public EnterpriseUserPo findByPhone(String phone) {
        return thirdUserDao.findByPhone(phone);
    }

    @Override
    public EnterpriseUserPo save(EnterpriseUserPo EnterpriseUserPo) {
        return thirdUserDao.save(EnterpriseUserPo);
    }

    @Override
    public void delete(Long id) {
        thirdUserDao.delete(id);
    }
}
