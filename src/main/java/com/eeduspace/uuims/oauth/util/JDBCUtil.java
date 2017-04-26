package com.eeduspace.uuims.oauth.util;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: dingran
 * Date: 2016/4/20
 * Description:
 */
@Component
public class JDBCUtil {

    private static final Logger logger = LoggerFactory.getLogger(JDBCUtil.class);
    private Gson gson=new Gson();
    @Inject
    private DataSource iwrongDataSource;

    public UserInfoModel getUserInfo(String userCode){
        UserInfoModel userModel=null;
        Connection conn= null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            conn= getConnection();
            String code="'"+userCode+"'";
            preparedStatement= conn.prepareStatement("SELECT * FROM userinfo u WHERE u.ctb_code="+code);
            resultSet=preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();

                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = resultSetMetaData.getColumnLabel(i).toUpperCase();// 获取别名
                    Object columnValue = resultSet.getObject(i);
                    logger.debug("------------columnName--->"+columnName);
                    logger.debug("------------columnValue--->"+columnValue);
                    map.put(column2field(columnName), columnValue);
                }
                String mapStr= gson.toJson(map);
                if(StringUtils.isNotBlank(mapStr)){
                 userModel= gson.fromJson(mapStr,UserInfoModel.class);
                }
            }

            logger.info("authentication center init successful!");

        } catch (Exception e) {
            logger.error("authentication center init error!", e);
        }finally {
            // 关闭resultSet资源  关闭preparedStatement资源  关闭connection资源
            close(resultSet,preparedStatement,conn);
        }
        return userModel;
    }


    public static String column2field(String columnName) {
        String[] arrays = columnName.toLowerCase().split("_");
        String propertyName = "";
        if (arrays.length > 0) {
            propertyName = arrays[0];
        }
        for (int i = 1; i < arrays.length; i++) {
            propertyName += (arrays[i].substring(0, 1).toUpperCase() + arrays[i].substring(1, arrays[i].length()));
        }
        return propertyName;
    }



    /**
     * 关闭连接资源
     *
     * @param objs
     *            含有colse()方法的对象集合
     *
     */
    public static void close(Object... objs) {
        for (Object obj : objs) {
            try {
                if (obj != null) {
                    Method method = obj.getClass().getMethod("close");
                    if (method != null) {
                        method.invoke(obj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return iwrongDataSource.getConnection();
    }
}
