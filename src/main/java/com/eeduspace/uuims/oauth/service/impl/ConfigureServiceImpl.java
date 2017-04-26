package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.ConfigureDao;
import com.eeduspace.uuims.oauth.persist.po.ConfigurePo;
import com.eeduspace.uuims.oauth.service.ConfigureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:系统配置管理
 */
@Service
public class ConfigureServiceImpl implements ConfigureService {

    private final Logger logger = LoggerFactory.getLogger(ConfigureServiceImpl.class);

    @Inject
    private ConfigureDao configureDao;

    @Override
    public List<ConfigurePo> findAll() {
        return (List<ConfigurePo>) configureDao.findAll();
    }

    @Override
    public ConfigurePo findOne(Long configureId) {
        return configureDao.findOne(configureId);
    }

    @Override
    public ConfigurePo save(ConfigurePo ConfigurePo) {
        return configureDao.save(ConfigurePo);
    }

    @Override
    public void delete(Long id) {
        configureDao.delete(id);
    }
}
