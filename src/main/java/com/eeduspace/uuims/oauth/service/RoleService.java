package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.RolePo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:角色管理
 */
public interface RoleService {

    /**
     * 查询所有
     * @return
     */
    List<RolePo> findAll();

    /**
     * 更加管理员查找
     * @param managerPo
     * @return
     */
    List<RolePo> findAllByManager(ManagerPo managerPo);
    /**
     * 更加管理员查找
     * @param managerPo
     * @return
     */
    List<RolePo> findAllByManager(ManagerPo managerPo,RoleEnum.Status status);
    /**
     * 查找
     * @param roleId
     * @return
     */
    RolePo findOne(Long roleId);
    /**
     * 查找
     * @param uuid
     * @return
     */
    RolePo findByUuid(String uuid);
    /**
     *
     * @param type
     * @return
     */
    RolePo findByType(RoleEnum.Type type);
    /**
     *
     * @param status
     * @return
     */
    List<RolePo> findByStatus(RoleEnum.Status status);

    /**
     * 新增/更新
     * @param RolePo
     * @return
     */
    RolePo save(RolePo RolePo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);
    /**
     * 删除所有
     */
    void deleteAll();
}
