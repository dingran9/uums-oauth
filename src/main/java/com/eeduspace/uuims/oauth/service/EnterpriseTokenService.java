package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.EnterpriseTokenPo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:第三方企业Token管理
 */
public interface EnterpriseTokenService {

    /**
     * 查询所有
     * @return
     */
    List<EnterpriseTokenPo> findAll();
    /**
     * 查找
     * @param enterpriseTokenId
     * @return
     */
    EnterpriseTokenPo findOne(Long enterpriseTokenId);

    /**
     * 新增/更新
     * @param EnterpriseTokenPo
     * @return
     */
    EnterpriseTokenPo save(EnterpriseTokenPo EnterpriseTokenPo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);
}
