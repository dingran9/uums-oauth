package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;

/**
 * Author: dingran
 * Date: 2015/11/2
 * Description:
 */
public interface AclService {

    /**
     * 判断是否是系统管理员
     * @param managerPo
     * @return
     */
    boolean isSystem(ManagerPo managerPo);

    /**
     * 判断是否是系统管理员
     * @param managerPo
     * @return
     */
    boolean isManager(ManagerPo managerPo);

    /**
     * 判断用户是否可被管理员管理
     * @param managerPo
     * @param userPo
     * @return
     */
    boolean isHasPermission(ManagerPo managerPo,UserPo userPo);

    /**
     * 判断普通管理员是否可被管理员管理
     * @param managerPo
     * @param managerPo1
     * @return
     */
    boolean isHasPermission(ManagerPo managerPo,ManagerPo managerPo1);
    /**
     * 判断管理员是否可管理产品
     * @param managerPo
     * @param productPo
     * @return
     */
    boolean isHasPermission(ManagerPo managerPo,ProductPo productPo);

    /**
     * 判断用户是否属于产品
     * @param userPo
     * @param productPo
     * @return
     */
    boolean isUserBelongProduct(UserPo userPo,ProductPo productPo);
}
