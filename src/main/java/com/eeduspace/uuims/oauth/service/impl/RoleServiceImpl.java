package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.RoleDao;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.RolePo;
import com.eeduspace.uuims.oauth.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:角色管理
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Inject
    private RoleDao roleDao;

    @Override
    public List<RolePo> findAll() {
        return (List<RolePo>) roleDao.findAll();
    }

    @Override
    public List<RolePo> findAllByManager(ManagerPo managerPo) {
        switch (managerPo.getRolePo().getType()){
            case Test:
                return  (List<RolePo>) roleDao.findAll();
            case System:
                return  (List<RolePo>) roleDao.findAll();
            case Product:
                List<RolePo> list=new ArrayList<>();
                list.add(roleDao.findByType(RoleEnum.Type.Product));
                return list;
            case Ordinary:

            default:
                return null;
        }
    }

    @Override
    public List<RolePo> findAllByManager(ManagerPo managerPo, RoleEnum.Status status) {
        switch (managerPo.getRolePo().getType()){
            case Test:
                return  (List<RolePo>) roleDao.findByStatus(status);
            case System:
                return  (List<RolePo>) roleDao.findByStatus(status);
            case Product:
                List<RolePo> list=new ArrayList<>();
                list.add(roleDao.findByTypeAndStatus(RoleEnum.Type.Product, status));
                return list;
            case Ordinary:

            default:
                return null;
        }
    }

    @Override
    public RolePo findOne(Long roleId) {
        return roleDao.findOne(roleId);
    }

    @Override
    public RolePo findByUuid(String uuid) {
        return roleDao.findByUuid(uuid);
    }

    @Override
    public RolePo findByType(RoleEnum.Type type) {
        return roleDao.findByType(type);
    }

    @Override
    public List<RolePo> findByStatus(RoleEnum.Status status) {
        return roleDao.findByStatus(status);
    }

    @Override
    public RolePo save(RolePo RolePo) {
        return roleDao.save(RolePo);
    }

    @Override
    public void delete(Long id) {
        roleDao.delete(id);
    }

    @Override
    public void deleteAll() {
        roleDao.deleteAll();
    }
}
