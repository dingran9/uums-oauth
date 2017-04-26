package com.eeduspace.uuims.oauth.persist.enumeration;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:用户枚举
 */
public class UserEnum {

    private Status status;
    private LoginStatus loginStatus;
    private Sex sex;
//    private BandStatus bandStatus;
    private CreateType createType;
    
    private ReviewStatus reviewStatus;//短信验证码审核状态
    

    public enum Status {
        NoActive(0),
        Disable(1),
        Enable(2),
        IsDelete(3);
        private final int value;

        public int getValue() {
            return value;
        }

        Status(int value) {
            this.value = value;
        }
    }
    public enum LoginStatus {
        UnKnow(0),
        IsLogout(1),
        IsLogin(2);
        private final int value;

        public int getValue() {
            return value;
        }

        LoginStatus(int value) {
            this.value = value;
        }
    }
    public enum Sex {
        UnKnow(0),
        Man(1),
        Woman(2);
        private final int value;

        public int getValue() {
            return value;
        }

        Sex(int value) {
            this.value = value;
        }
    }
    
    
/*
    public enum BandStatus {
        NoBand(0),
        BandTencent(1),
        BandWechat(2),
        AllBand(3);
        private final int value;

        public int getValue() {
            return value;
        }

        BandStatus(int value) {
            this.value = value;
        }
    }
*/

    /**
     * 创建类型： 0：用户注册 1：管理员创建
     */
    public enum CreateType {
        ManagerAdd(0),//管理员新增方式
        TemplateAdd(1);//模板新增方式

        private final int value;

        public int getValue() {
            return value;
        }

        CreateType(int value) {
            this.value = value;
        }
    }

    /**
     * 发短信验证码的状态
     * @author Administrator
     *
     */
    public enum ReviewStatus {
        UnReview(0),//未审核
        ReviewAdopt(1);//审核通过
        private final int value;

        public int getValue() {
            return value;
        }

        ReviewStatus(int value) {
            this.value = value;
        }
    }
    
    
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }
/*
    public BandStatus getBandStatus() {
        return bandStatus;
    }

    public void setBandStatus(BandStatus bandStatus) {
        this.bandStatus = bandStatus;
    }*/

    public CreateType getCreateType() {
        return createType;
    }

    public void setCreateType(CreateType createType) {
        this.createType = createType;
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
    }
}
