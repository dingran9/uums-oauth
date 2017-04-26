package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.TokenPo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:令牌管理
 */
public interface TokenService {

    /**
     * 查询所有
     * @return
     */
    List<TokenPo> findAll();

    /**
     * 根据用户ID 及产品ID 及设备类型获取
     * @return
     */
    List<TokenPo> findByOpenIdAndPIdAndEquipmentType(String openId,Long productId,SourceEnum.EquipmentType type);
    /**
     * 查找
     * @param tokenId
     * @return
     */
    TokenPo findOne(Long tokenId);

    /**
     *
     * @param refreshToken
     * @return
     */
    TokenPo findByRefreshToken(String refreshToken);

    /**
     * 新增/更新
     * @param TokenPo
     * @return
     */
    TokenPo save(TokenPo TokenPo);
    /**
     * 新增/更新
     * @param TokenPo
     * @return
     */
    TokenPo save(TokenPo TokenPo,TokenPo deletePo);

    /**
     * 新增/更新
     * @param TokenPo
     * @return
     */
    TokenPo save(TokenPo TokenPo,String openId);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);


    /**
     * 删除
     * @param tokenPo
     */
    void delete(TokenPo tokenPo);
}
