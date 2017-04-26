package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.EnterpriseDao;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.EnterprisePo;
import com.eeduspace.uuims.oauth.service.EnterpriseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:第三方企业管理
 */
@Service
public class EnterpriseServiceImpl implements EnterpriseService {

    private final Logger logger = LoggerFactory.getLogger(EnterpriseServiceImpl.class);

    @Inject
    private EnterpriseDao enterpriseDao;

    @Override
    public List<EnterprisePo> findAll() {
        return (List<EnterprisePo>) enterpriseDao.findAll();
    }

    @Override
    public EnterprisePo findOne(Long enterpriseId) {
        return enterpriseDao.findOne(enterpriseId);
    }

    @Override
    public EnterprisePo findByEnterpriseType (SourceEnum.EnterpriseType type) {
        return enterpriseDao.findByEnterpriseType(type);
    }

    @Override
    public EnterprisePo save(EnterprisePo EnterPrisePo) {
        return enterpriseDao.save(EnterPrisePo);
    }

    @Override
    public void delete(Long id) {
        enterpriseDao.delete(id);
    }

    @Override
    public void deleteAll() {
        enterpriseDao.deleteAll();
    }
}
