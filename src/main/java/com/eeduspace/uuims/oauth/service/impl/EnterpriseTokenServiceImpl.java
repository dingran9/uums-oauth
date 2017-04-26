package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.EnterpriseTokenDao;
import com.eeduspace.uuims.oauth.persist.po.EnterpriseTokenPo;
import com.eeduspace.uuims.oauth.service.EnterpriseTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:第三方企业令牌管理
 */
@Service
public class EnterpriseTokenServiceImpl implements EnterpriseTokenService {

    private final Logger logger = LoggerFactory.getLogger(EnterpriseTokenServiceImpl.class);

    @Inject
    private EnterpriseTokenDao enterpriseTokenDao;

    @Override
    public List<EnterpriseTokenPo> findAll() {
        return (List<EnterpriseTokenPo>) enterpriseTokenDao.findAll();
    }

    @Override
    public EnterpriseTokenPo findOne(Long enterpriseTokenId) {
        return enterpriseTokenDao.findOne(enterpriseTokenId);
    }

    @Override
    public EnterpriseTokenPo save(EnterpriseTokenPo EnterpriseTokenPo) {
        return enterpriseTokenDao.save(EnterpriseTokenPo);
    }

    @Override
    public void delete(Long id) {
        enterpriseTokenDao.delete(id);
    }
}
