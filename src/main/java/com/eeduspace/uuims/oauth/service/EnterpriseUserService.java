package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.EnterpriseUserPo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:第三方用户管理
 */
public interface EnterpriseUserService {

    /**
     * 查询所有
     * @return
     */
    List<EnterpriseUserPo> findAll();
    /**
     * 查找
     * @param thirdUserId
     * @return
     */
    EnterpriseUserPo findOne(Long thirdUserId);

    /**
     * 查找
     * @param openId
     * @return
     */
    EnterpriseUserPo findByOpenId(String openId);

    /**
     * 查找
     * @param phone
     * @return
     */
    EnterpriseUserPo findByPhone(String phone);
    /**
     * 新增/更新
     * @param EnterpriseUserPo
     * @return
     */
    EnterpriseUserPo save(EnterpriseUserPo EnterpriseUserPo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);
}
