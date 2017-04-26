package com.eeduspace.uuims.oauth.persist.enumeration;

/**
 * Author: dingran
 * Date: 2015/11/3
 * Description:
 */
public class RoleEnum {

    private Type type;
    private Status status;

    public enum Type {
        Test(0),
        Ordinary(1),//普通管理员
        Product(2),//产品管理员
        System (3);//系统管理员
        private final int value;

        public int getValue() {
            return value;
        }

        Type(int value) {
            this.value = value;
        }
    }
    public enum Status {
        Enable(0),
        Disable(1),
        IsDelete(2);
        private final int value;

        public int getValue() {
            return value;
        }

        Status(int value) {
            this.value = value;
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
