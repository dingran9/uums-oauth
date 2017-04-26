package com.eeduspace.uuims.oauth.exception;

/**
 * Author: dingran
 * Date: 2016/5/4
 * Description:
 */
public class TokenException extends RuntimeException{

    private final String classN;

    public TokenException(String message) {
        super(message);
        this.classN = TokenException.class.getName();
    }


    public TokenException(String message, Throwable e) {
        super(message, e);
        this.classN = e.getClass().getName();
    }

    public TokenException(Throwable e) {
        super(e.getMessage());
        this.classN = e.getClass().getName();
    }

    public String getClassN() {
        return classN;
    }


}
