package com.eeduspace.uuims.oauth.persist.dao;


import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:用户Dao管理
 */
public interface UserDao extends CrudRepository<UserPo, Long> {
	
	

    List<UserPo> findByStatus(UserEnum.Status state);

    /**
     * 通过名称查找用户
     * @param phone 用户手机号
     * @return
     */
    UserPo findByPhone(String phone);
    /**
     * 通过名称查找用户
     * @param schoolNumber 用户手机号
     * @return
     */
    UserPo findBySchoolNumber(String schoolNumber);

    /**
     * 通过ak查找用户
     * @param AccessKeyId 用户ak
     * @return
     */
    @Query("select l from  UserPo l  where l.accessKey=?1 ")
    UserPo findByAccessKey(String AccessKeyId);

    /**
     * 查找用户
     * @param uuid 用户uuid
     * @return
     */
    UserPo findByUuid(String uuid);

    /**
     * 分页获取
     * @param spec
     * @param pageable
     * @return
     */
    @Query("select l from  UserPo l  where l.status in (0,1,2)")
    Page<UserPo> findAll(Specification<UserPo> spec, Pageable pageable);

    @Query("select u from UserPo u ,ProductUserPo pu where pu.productId=?1 and u.id=pu.userId and u.status in (0,1,2)")
    PageImpl<UserPo> findPageByProductId(Long productId,Pageable pageable);

    @Query("select u from UserPo u ,ProductUserPo pu where pu.productId=?1 and u.status=?2 and u.id=pu.userId")
    PageImpl<UserPo> findPageByProductId(Long productId,UserEnum.Status status,Pageable pageable);

    UserPo findByEmail(String email);

//    @Query("select a.b from A a where a.c.id = ?1")
//    Page<UserPo> findAll(Long cid, Pageable pageRequest);


    @Query("select l from  UserPo l  where l.registerProductId=?1 and l.status in (0,1,2)")
    List<UserPo> findByProductId(Long productId);
   
    /**
     * 添加根据productId和uuid集合来查询用户列表
     * */
    List<UserPo> findByRegisterProductIdAndUuidIn(Long productId,List<String> uuids);

}
