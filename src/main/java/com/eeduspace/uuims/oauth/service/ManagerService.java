package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:管理员管理
 */
public interface ManagerService {
    /**
     * 查找用户
     * @param managerId
     * @return
     */
    ManagerPo findOne(Long managerId);
    /**
     * 查找用户
     * @param phone
     * @return
     */
    ManagerPo findByPhone(String phone);
    /**
     * 查找用户
     * @param AccessKeyId
     * @return
     */
    ManagerPo findByAccessKeyId(String AccessKeyId);

    /**
     * 查找用户
     * @param uuid
     * @return
     */
    ManagerPo findByUuid(String uuid);

    /**
     * 新增/更新用户
     * @param ManagerPo
     * @return
     */
    ManagerPo save(ManagerPo ManagerPo);

    /**
     * 删除用户
     * @param id
     */
    void delete(Long id);

    /**
     * 删除所有用户
     */
    void deleteAll();

    /**
     * 验证登录用户是否存在
     * @param phone 登陆用户
     * @param password MD5加密过的密码
     * @return
     */
    ManagerPo validateUser(String phone, String password);

    /**
     * 查询所有
     * @return
     */
    List<ManagerPo> findAll();

    /**
     * 按照管理员查询
     * @return
     */
    List<ManagerPo> findAllByManager(ManagerPo managerPo) ;
    /**
     * 按照管理员查询
     * @return
     */
    List<ManagerPo> findByProductId(Long product) ;
    /**
     * 按照管理员查询
     * @return
     */
    List<ManagerPo> findAllByManager(ManagerPo managerPo,UserEnum.Status status) ;

    /**
     * 获取分页
     * @param type
     * @param keyword
     * @param param
     * @param sort
     * @param request
     * @return
     */
    Page<ManagerPo> findPage(String type,String keyword,String param,String sort,PageRequest request);

    /**
     * 获取分页
     * @param managerPo
     * @param type
     * @param keyword
     * @param param
     * @param sort
     * @param request
     * @return
     */
    Page<ManagerPo> findPage(ManagerPo managerPo,String type,String keyword,String param,String sort,PageRequest request);



}

