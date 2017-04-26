package com.eeduspace.uuims.oauth.service;

import com.eeduspace.uuims.comm.util.base.CommandUtil;
import com.eeduspace.uuims.oauth.persist.po.RolePo;
import com.ibatis.common.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:项目启动初始化
 */
public class InitAuthService {
    private static final Logger logger = LoggerFactory.getLogger(InitAuthService.class);
    //初始化角色
    //初始化管理员
    //初始化产品应用
    //初始化第三方
    //初始化系统配置
    @Inject
    private ManagerService managerService;
    @Inject
    private RoleService roleService;
    @Inject
    private EnterpriseService enterpriseService;
    @Inject
    private DataSource dataSource;
    @Value("${uuims.init.sql.path}")
    private String sqlPath;
    @PostConstruct
    @Transactional
    public void init() {
        try {
            List<RolePo> rolePos=  roleService.findAll();
            if(rolePos.size()!=0 ){
                return;
            }
            //初始化权限
            roleService.deleteAll();
            managerService.deleteAll();
            enterpriseService.deleteAll();
            // uuims_oauth.sh文件 初始化 操作权限、角色、
            Connection conn = getConnection();
            ScriptRunner runner = new ScriptRunner(conn, false, false);
            runner.setErrorLogWriter(null);
            runner.setLogWriter(null);
//            runner.runScript(Resources.getResourceAsReader("conf/uuims-oauth.sql"));
            runner.runScript(new InputStreamReader(new FileInputStream(sqlPath),"UTF-8"));//防止读取时中文乱码
            conn.close();
            logger.info("authentication center init successful!");
        } catch (Exception e) {
            logger.error("authentication center init error!", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    //@Test
    public void test() {
        try {
            // executeShell(sendKondorShellName);
            String res=   CommandUtil.exec("", "cmd /c start *.sh");
            logger.info(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
