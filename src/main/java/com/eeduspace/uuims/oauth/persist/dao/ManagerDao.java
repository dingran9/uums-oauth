package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface ManagerDao  extends CrudRepository<ManagerPo, Long> {
    /**
     * 通过名称查找用户
     * @param phone 用户手机号
     * @return
     */
    ManagerPo findByPhone(String phone);
    /**
     * 通过名称查找用户
     * @param phone 用户手机号
     * @return
     */
    @Query("select l from  ManagerPo l  where l.phone=?1 and l.password=?2")
    ManagerPo findByPhoneAndPassword(String phone,String password);
    /**
     * 通过ak查找用户
     * @param AccessKeyId 用户ak
     * @return
     */
    @Query("select l from  ManagerPo l  where l.accessKey=?1 ")
    ManagerPo findByAccessKey(String AccessKeyId);

    /**
     * 查找用户
     * @param uuid 用户uuid
     * @return
     */
    ManagerPo findByUuid(String uuid);

    @Query("select l from  ManagerPo l  where l.productPo.id=?1 and l.status in (0,1,2)")
    List<ManagerPo> findByProductId(Long productId);

    @Query("select l from  ManagerPo l  where l.productPo.id=?1 and l.status=?2")
    List<ManagerPo> findByProductId(Long productId,UserEnum.Status status);

    @Query("select l from  ManagerPo l  where l.status=?1")
    List<ManagerPo> findByStatus(UserEnum.Status status);


    /**
     * 分页获取
     * @param spec
     * @param pageable
     * @return
     */
    @Query("select l from  ManagerPo l  where l.status in (0,1,2)")
    Page<ManagerPo> findAll(Specification<ManagerPo> spec, Pageable pageable);

}
