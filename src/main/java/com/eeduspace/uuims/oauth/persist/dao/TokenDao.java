package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.TokenPo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface TokenDao extends CrudRepository<TokenPo, Long>  {

    TokenPo  findByRefreshToken(String refreshToken);

    @Modifying
    @Query("delete from TokenPo t where t.openId=?1")
    void deleteByOpenId(String openId);

    List<TokenPo> findByOpenIdAndProductIdAndEquipmentType(String openId,Long productId,SourceEnum.EquipmentType type);

    @Query("select t from TokenPo t where t.openId=?1 and t.productId=?2 and (t.equipmentType=2 or t.equipmentType=3)")
    List<TokenPo> findByOpenIdAndProductIdAndEquipmentType(String openId,Long productId);
}
