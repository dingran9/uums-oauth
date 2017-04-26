package com.eeduspace.uuims.oauth.persist.enumeration;

/**
 * Author: dingran
 * Date: 2015/11/3
 * Description:
 */
public class EnterpriseEnum {

    private Type type;

    public enum Type {
        A(0),//我方为甲方
        B(1);//我方为乙方
        private final int value;

        public int getValue() {
            return value;
        }

        Type(int value) {
            this.value = value;
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
