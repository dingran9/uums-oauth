package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.EnterprisePo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:第三方企业管理
 */
public interface EnterpriseService {

    /**
     * 查询所有
     * @return
     */
    List<EnterprisePo> findAll();
    /**
     * 查找
     * @param enterpriseId
     * @return
     */
    EnterprisePo findOne(Long enterpriseId);
    /**
     * 查找
     * @param type
     * @return
     */
    EnterprisePo findByEnterpriseType(SourceEnum.EnterpriseType type);
    /**
     * 新增/更新
     * @param EnterPrisePo
     * @return
     */
    EnterprisePo save(EnterprisePo EnterPrisePo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);

    /**
     * 删除
     */
    void deleteAll();
}
