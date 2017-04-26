package com.eeduspace.uuims.oauth.persist.enumeration;

/**
 * Author: dingran
 * Date: 2015/11/9
 * Description:
 */
public class TokenEnum {

    private Type type;

    public enum Type {
        User(0),
        Manager(1);
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
