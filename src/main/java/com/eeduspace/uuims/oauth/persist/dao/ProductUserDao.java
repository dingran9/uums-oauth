package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ProductUserPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/11/2
 * Description:
 */
public interface ProductUserDao  extends CrudRepository<ProductUserPo, Long> {

    @Query("select u from UserPo u,ProductUserPo pu where pu.productId=?1 and  pu.userId=u.id")
    List<UserPo> findByProductId(Long productId);

    @Query("select u from UserPo u,ProductUserPo pu where pu.productId=?1 and  pu.userId=u.id and u.status=?2")
    List<UserPo> findByProductId(Long productId,UserEnum.Status status);

    @Query("select p from ProductUserPo p where  p.userId=?1 and p.productId=?2")
    List<ProductUserPo> findByUserIdAndProductId(Long userId,Long productId);

    @Modifying
    @Query("delete from  ProductUserPo pu where pu.productId=?1")
    void deleteByProductId(Long productId);

    @Modifying
    @Query("delete from  ProductUserPo pu where pu.userId=?1")
    void deleteByUserId(Long userId);
}
