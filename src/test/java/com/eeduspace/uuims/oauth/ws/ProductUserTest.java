package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.BaseTest;
import com.eeduspace.uuims.oauth.persist.dao.ProductUserDao;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.google.gson.Gson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/11/9
 * Description:
 */
public class ProductUserTest extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(ProductUserTest.class);

    private Gson gson=new Gson();
    @Inject
    private ProductUserDao productUserDao;
    @Test
    public void  test(){

        List<UserPo> list1= productUserDao.findByProductId(1l);
        List<UserPo> list2= productUserDao.findByProductId(1l, UserEnum.Status.Enable);
        
        logger.debug(gson.toJson(list1));
        logger.debug("----------------list1-->"+list1.size());
        logger.debug(gson.toJson(list2));
        logger.debug("----------------list2-->"+list2.size());
    }

}
