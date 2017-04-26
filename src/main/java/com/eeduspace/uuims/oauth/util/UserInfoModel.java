package com.eeduspace.uuims.oauth.util;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2016/4/21
 * Description:
 */
public class UserInfoModel implements Serializable{

    protected Long id;
    protected String ctbCode;
    protected String code;
    protected long createAt ;
    protected String createAuthor;
    protected long updateAt ;
    protected String updateAuthor;

    private String email;
    private String mobile;
    private String userName;
    private String nickName;
    private String password;
    private String realName;
    private int sex;   //性别 0男1女
    private String provinceCode;
    private String cityCode;
    private String areaCode;
    private String stageCode;
    private String gradeCode;   //年级标识
    private String schoolCode;
    private String classCode;
    private String registerSource;  //注册来源6:web端,7:android端,8:ios,0:开发测试
    private int type;  //用户类别:6自己的用户,7电信用户,8移动用户,0开发测试假数据
    private int status;  //0正常,1禁用...
    private int version;  //数据版本
    private String imagePath;   //头像路径
    private String cardId;      //身份证号码
    private int bdStatus;    //绑定状态0未绑定,1绑定手机,2绑定邮箱,3全部绑定


    private String uuid;
    private String userCode;//用户唯一标识
    private boolean isVIP;//是否是vip
    private Date  VIPExpireTime;//vip到期时间
    private String marketChannel;
    private String registerDeviceType;
    private Date createDate;
    private Date updateDate;
    private String accessKey;
    private String secretKey;
    private String openId;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCtbCode() {
        return ctbCode;
    }

    public void setCtbCode(String ctbCode) {
        this.ctbCode = ctbCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public String getCreateAuthor() {
        return createAuthor;
    }

    public void setCreateAuthor(String createAuthor) {
        this.createAuthor = createAuthor;
    }

    public long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(long updateAt) {
        this.updateAt = updateAt;
    }

    public String getUpdateAuthor() {
        return updateAuthor;
    }

    public void setUpdateAuthor(String updateAuthor) {
        this.updateAuthor = updateAuthor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getStageCode() {
        return stageCode;
    }

    public void setStageCode(String stageCode) {
        this.stageCode = stageCode;
    }

    public String getGradeCode() {
        return gradeCode;
    }

    public void setGradeCode(String gradeCode) {
        this.gradeCode = gradeCode;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getRegisterSource() {
        return registerSource;
    }

    public void setRegisterSource(String registerSource) {
        this.registerSource = registerSource;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public int getBdStatus() {
        return bdStatus;
    }

    public void setBdStatus(int bdStatus) {
        this.bdStatus = bdStatus;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public boolean isVIP() {
        return isVIP;
    }

    public void setVIP(boolean isVIP) {
        this.isVIP = isVIP;
    }

    public Date getVIPExpireTime() {
        return VIPExpireTime;
    }

    public void setVIPExpireTime(Date VIPExpireTime) {
        this.VIPExpireTime = VIPExpireTime;
    }

    public String getMarketChannel() {
        return marketChannel;
    }

    public void setMarketChannel(String marketChannel) {
        this.marketChannel = marketChannel;
    }

    public String getRegisterDeviceType() {
        return registerDeviceType;
    }

    public void setRegisterDeviceType(String registerDeviceType) {
        this.registerDeviceType = registerDeviceType;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
}
