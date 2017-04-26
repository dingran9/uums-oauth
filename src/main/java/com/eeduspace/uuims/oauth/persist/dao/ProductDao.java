package com.eeduspace.uuims.oauth.persist.dao;

import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import org.springframework.data.repository.CrudRepository;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public interface ProductDao  extends CrudRepository<ProductPo, Long> {

    ProductPo findByName(String name);

    ProductPo findByType(String type);

    ProductPo findByUuid(String uuid);
}
