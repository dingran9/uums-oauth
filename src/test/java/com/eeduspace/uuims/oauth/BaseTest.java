package com.eeduspace.uuims.oauth;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * Created by zn on 13-12-22.
 */
@ContextConfiguration(locations = {"classpath*:*/**/spring-*.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseTest {
    @Inject
    protected AbstractApplicationContext context;
 /*   @Inject
    protected Gson gson;*/

    @Before
    public void init() {
//        context = new ClassPathXmlApplicationContext("classpath*:/conf/**/spring-*.xml"
//                , "classpath*:/test/**/spring-*.xml");
    }

    protected final Logger logger = LoggerFactory.getLogger(BaseTest.class);
}
