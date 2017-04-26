package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.ConfigurePo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:系统配置管理
 */
public interface ConfigureService {

    /**
     * 查询所有
     * @return
     */
    List<ConfigurePo> findAll();
    /**
     * 查找用户
     * @param configureId
     * @return
     */
    ConfigurePo findOne(Long configureId);

    /**
     * 新增/更新用户
     * @param ConfigurePo
     * @return
     */
    ConfigurePo save(ConfigurePo ConfigurePo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);
}
