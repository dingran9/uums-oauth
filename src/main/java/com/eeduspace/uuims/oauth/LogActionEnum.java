package com.eeduspace.uuims.oauth;

/**
 * Author: dingran
 * Date: 2015/12/2
 * Description:
 */
public enum LogActionEnum {

    LOGIN("login"),
    MANAGER_LOGIN("manager_login"),
    LDAP("ldap"),
    LOGOUT("logout"),
    LOGOUT_MANAGER("logout_manager"),

    ACTIVATION("activation"),
    GET_KEY("get_key"),
    GET_TOKEN("get_token"),
    UPDATE_SECRET_KEY("update_secret_key"),

    GET_KEY_MANAGER("get_key_manager"),
    GET_TOKEN_MANAGER("get_token_manager"),
    UPDATE_SECRET_KEY_MANAGER("update_secret_key_manager"),
    REFRESH_TOKEN("refresh_token"),

    LIST("list"),
    PAGE_LIST("pageList"),
    DESCRIBE("describe"),
    CREATE("create"),
    EDIT_PASSWORD("edit_password"),
    RESET_PASSWORD("reset_password"),
    UPDATE("update"),
    UPDATE_STATUS("update_status"),
    DELETE("delete"),
    VALIDATE("validate"),

/*    LIST_MANAGER("list_manager"),
    PAGE_LIST_MANAGER("pageList_manager"),
    DESCRIBE_MANAGER("describe_manager"),
    CREATE_MANAGER("create_manager"),
    EDIT_PASSWORD_MANAGER("edit_password_manager"),
    RESET_PASSWORD_MANAGER("reset_password_manager"),
    UPDATE_MANAGER("update_manager"),
    UPDATE_STATUS_MANAGER("update_status_manager"),
    DELETE_MANAGER("delete_manager"),
    VALIDATE_MANAGER("validate_manager"),

    LIST_ROLE("list_role"),
    DESCRIBE_ROLE("describe"),
    UPDATE_ROLE("update"),
    UPDATE_STATUS_ROLE("update_status"),*/

    AUTHORIZATION("authorization"),
    BIND("bind"),
    BIND_PHONE("bind_phone"),
    BIND_EMAIL("bind_email"),
    BIND_TENCENT("bind_tencent"),
    BIND_WECHAT("bind_wechat"),
    BIND_SINA("bind_sina"),
    AUTHORIZE_WECHAT("authorize_WeChat"),
    AUTHORIZE_TENCENT("authorize_tencent"),
    AUTHORIZE_SINA("authorize_sina"),


    USER("user"),
    MANAGER("manager"),
    ROLE("role"),
    REDIS("redis"),
    PRODUCT("product"),
    ENTERPRISE("enterprise"),
    AUTHORIZE("authorize"),
    ACCESS("access"),
    TOKEN("token"),
    SEND_SMS("send_sms"),
    REMOTE_ADDRESS("remote_address"),
    ;

    private String name;

    private LogActionEnum(String name) {
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
