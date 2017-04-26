package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.comm.util.base.DateUtils;
import com.eeduspace.uuims.comm.util.base.encrypt.Digest;
import com.eeduspace.uuims.oauth.persist.dao.ProductDao;
import com.eeduspace.uuims.oauth.persist.dao.ProductUserDao;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.service.ProductService;
import com.eeduspace.uuims.oauth.service.RoleService;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:产品管理
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Inject
    private ProductDao productDao;
    @Inject
    private ProductUserDao productUserDao;
    @Inject
    private RoleService roleService;
    @Inject
    private ManagerService managerService;

    @Value("${uuims.product.manager.password}")
    private String pwd;
    @Override
    public List<ProductPo> findAll() {
        return (List<ProductPo>) productDao.findAll();
    }

    @Override
    public ProductPo findOne(Long productId) {
        return productDao.findOne(productId);
    }

    @Override
    public ProductPo findByType(String type) {
        return productDao.findByType(type);
    }

    @Override
    public ProductPo findByName(String name) {
        return productDao.findByName(name);
    }

    @Override
    public ProductPo findByUuid(String uuid) {
        return productDao.findByUuid(uuid);
    }

    //TODO 添加该产品下的管理员
    @Override
    @Transactional
    public ProductPo create(ProductPo productPo) {
        productPo=  productDao.save(productPo);
        ManagerPo managerPo=new ManagerPo();
        managerPo.setName(productPo.getName()+"@admin");
        managerPo.setPassword(Digest.md5Digest(pwd));
        managerPo.setRolePo(roleService.findByType(RoleEnum.Type.Product));
        managerPo.setProductPo(productPo);
        managerPo.setPhone(productPo.getName()+"@admin");
        String accessKeyId = "VE" + Digest.md5Digest16(productPo.getName()+"@admin" + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
        // 验证 AccessKeyId是否已存在
        ManagerPo ven = managerService.findByAccessKeyId(accessKeyId);
        if (ven != null) {
            accessKeyId = "VE" + Digest.md5Digest16(productPo.getName()+"@admin" + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6)).toUpperCase();
        }
        String secretKey = Digest.md5Digest(productPo.getName()+"@admin" + DateUtils.nowTimeMillis() + RandomUtils.getRandom(6) + accessKeyId);
        managerPo.setAccessKey(accessKeyId);
        managerPo.setSecretKey(secretKey);
        managerPo.setStatus(UserEnum.Status.Enable);
        managerService.save(managerPo);
        return productPo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productDao.delete(id);
        productUserDao.deleteByProductId(id);
    }

    @Override
    public ProductPo save(ProductPo productPo) {
        return productDao.save(productPo);
    }
}
