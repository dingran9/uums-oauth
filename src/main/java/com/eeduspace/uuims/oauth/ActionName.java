package com.eeduspace.uuims.oauth;

/**
 * Author: dingran
 * Date: 2015/10/21
 * Description:action转换
 */

public enum ActionName {
	/**用户注册统计*/
	USERREGISTERCOUNT("user_register_count"),
	/**用户活跃量统计*/
	USERACTIVECOUNT("user_active_count"),
	/**用户当天活跃量*/
	TODAYUSERACTIVECOUNT("today_user_active"),
	/**用户当天活跃量按产品分组*/
	TODAYUSERACTIVECOUNTBYPRODUCTID("today_user_active_by_productId"),
	/**用户当天注册量*/
	TODAYUSERREGISTERCOUNT("today_user_register"),
	/**用户当天注册量按产品分组*/
	TODAYUSERREGISTERCOUNTBYPRODUCTID("today_user_register_by_productId"),
	/**注册量汇总信息统计*/
	USERREGISTERTOTALCOUNT("total_count_user_register"),
	/**活跃量汇总信息统计*/
	USERACTIVETOTALCOUNT("total_count_user_active"),
	/**根据产品分组统计*/
	USERCOUNTBYPRODUCT("user_usage_count_by_product"),
	
    LOGIN("login"),
    MANAGER_LOGIN("manager_login"),
    LDAP("ldap"),
    LOGOUT("logout"),
    ACTIVATION("activation"),
    GET_KEY("get_key"),
    GET_TOKEN("get_token"),
    UPDATE_SECRET_KEY("update_secret_key"),
    TOKEN("token"),
    REFRESH_TOKEN("refresh_token"),
    LIST("list"),
    PAGE_LIST("pageList"),
    DESCRIBE("describe"),
    CREATE("create"),
    CREATE_LIST("create_list"),
    EDIT_PASSWORD("edit_password"),
    RESET_PASSWORD("reset_password"),
    UPDATE("update"),
    UPDATE_STATUS("update_status"),
    BATCH_UPDATE_STATUS("batch_update_status"),
    DELETE("delete"),
    BATCH_DELETE_USERS("batch_delete_users"),
    VALIDATE("validate"),
    AUTHORIZATION("authorization"),
    BIND("bind"),
    BIND_PHONE("bind_phone"),
    BIND_EMAIL("bind_email"),
    BIND_TENCENT("bind_tencent"),
    BIND_WECHAT("bind_wechat"),
    BIND_SINA("bind_sina"),
    AUTHORIZE("authorize"),
    AUTHORIZE_WECHAT("authorize_WeChat"),
    AUTHORIZE_TENCENT("authorize_tencent"),
    AUTHORIZE_SINA("authorize_sina"),
    GET_EXCEL_TEMPLATE("getExcelTemplate"),
    ADD_EXCEL_BY_TEMPLATE("addEmployeeByExcel"),
    MANAGER_LOGS("manager_logs"),
    USER_LOGS("user_logs"),
    SEND_SMS("send_sms"),
    VALIDATE_CODE("validate_code"),

    
    /**在线用户数量**/
    ONLINE_USER_SIZE("online_user_size"),
    /**在线用户列表**/
    ONLINE_USER_LIST("online_user_list"),
    /**在线用户全局概览数量**/
    ONLINE_USER_SIZE_ALL("online_user_size_all"),
    /**根据手机号查询用户详情**/
    DESCRIBE_BY_PHONE("describe_by_phone"),
    /**自会陪伴专用   手机号 查询用户    如果不存在就添加**/
    SEARCH_BY_PHONE("search_by_phone")
    ;



    private String name;

    private ActionName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }


    public static ActionName toEnum(String name) {
        for (ActionName an : ActionName.values()) {
            if (an.toString().equalsIgnoreCase(name)) {
                return an;
            }
        }
        return null;
    }

    public static void main(String[] args){
        System.out.println(ActionName.LOGIN.toString());
    }

}
