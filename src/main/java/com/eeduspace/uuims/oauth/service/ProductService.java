package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.oauth.persist.po.ProductPo;

import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:产品管理
 */
public interface ProductService {

    /**
     * 查询所有
     * @return
     */
    List<ProductPo> findAll();
    /**
     * 查找
     * @param productId
     * @return
     */
    ProductPo findOne(Long productId);

    /**
     *
     * @param type
     * @return
     */
    ProductPo findByType(String type);
    /**
     *
     * @param name
     * @return
     */
    ProductPo findByName(String name);
    /**
     *
     * @param uuid
     * @return
     */
    ProductPo findByUuid(String uuid);
    /**
     * 新增/更新
     * @param ProductPo
     * @return
     */
    ProductPo save(ProductPo ProductPo);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);

    ProductPo create(ProductPo productPo);
}
