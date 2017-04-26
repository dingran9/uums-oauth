package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.ProductUserDao;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.service.AclService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Author: dingran
 * Date: 2015/11/2
 * Description:
 */
@Service
public class AclServiceImpl implements AclService {


    @Inject
    private ProductUserDao productUserDao;

    /**
     * 判断是否是系统管理员
     * @param managerPo
     * @return
     */
    @Override
    public boolean isSystem(ManagerPo managerPo) {
        return managerPo != null && managerPo.getRolePo().getType().equals(RoleEnum.Type.System);
    }

    @Override
    public boolean isManager(ManagerPo managerPo) {
        return managerPo != null && (managerPo.getRolePo().getType().equals(RoleEnum.Type.System) || managerPo.getRolePo().getType().equals(RoleEnum.Type.Product));
    }

    /**
     * 判断用户是否可被管理员管理
     * @param managerPo
     * @param userPo
     * @return
     */
    @Override
    public boolean isHasPermission(ManagerPo managerPo, UserPo userPo) {
        if(managerPo.getRolePo().getType().equals(RoleEnum.Type.System)){
            return true;
        }
        return managerPo.getRolePo().getType().equals(RoleEnum.Type.Product) && managerPo.getProductPo() != null
                && productUserDao.findByUserIdAndProductId(userPo.getId(), managerPo.getProductPo().getId()) != null;
    }

    @Override
    public boolean isHasPermission(ManagerPo managerPo, ManagerPo managerPo1) {
        if(managerPo.getRolePo().getType().equals(RoleEnum.Type.System)){
            return true;
        }
 /*       if(managerPo1.getRolePo().getType().equals(RoleEnum.Type.Product)){
            return false;
        }*/
        return managerPo.getRolePo().getType().equals(RoleEnum.Type.Product) && managerPo.getProductPo().getId().equals(managerPo1.getProductPo().getId());
    }

    /**
     * 判断管理员是否可管理产品
     * @param managerPo
     * @param productPo
     * @return
     */
    @Override
    public boolean isHasPermission(ManagerPo managerPo, ProductPo productPo) {
        if(managerPo.getRolePo().getType().equals(RoleEnum.Type.System)){
            return true;
        }
        return managerPo.getRolePo().getType().equals(RoleEnum.Type.Product) && productPo.getId().equals(managerPo.getProductPo().getId());
    }

    /**
     * 判断用户是否属于产品
     * @param userPo
     * @param productPo
     * @return
     */
    @Override
    public boolean isUserBelongProduct(UserPo userPo, ProductPo productPo) {
        return productUserDao.findByUserIdAndProductId(userPo.getId(),productPo.getId())!=null;
    }
}
