package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.CodePo;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface CodeService {

    /**
     * 查询所有
     * @return
     */
    Iterable<CodePo> findAll();
    /**
     * 查找
     * @param codeId
     * @return
     */
    CodePo findOne(Long codeId);

    /**
     * 新增/更新
     * @param CodePo
     * @return
     */
    CodePo save(CodePo CodePo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);
}
