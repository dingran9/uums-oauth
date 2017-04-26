package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.enumeration.UserEnum;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:
 */
@Entity
@Table(name = "auth_user_info")
public class UserInfoPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ForeignKey(name = "none")
    @NotFound(action= NotFoundAction.IGNORE)
    private UserPo userPo;

    @Column(name = "register_source")
    private SourceEnum.EquipmentType registerSource;//注册的终端类型

    @Column(name = "create_type")
    private UserEnum.CreateType createType;//创建类型

/*
    @ManyToOne
    @JoinColumn(name = "manager_id", referencedColumnName = "id")
    @ForeignKey(name = "none")
    @NotFound(action= NotFoundAction.IGNORE)
    private ManagerPo managerPo;
*/
    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "nick_name")
    private String nickName;//昵称
    private UserEnum.Sex sex= UserEnum.Sex.UnKnow;//性别
    @Column(length = 1000)
    private String address;//地址
    @Column(name = "image_path")
    private String imagePath;//头像路径
    @Column(name = "card_id")
    private String cardId;//身份证ID
    @Column(name = "real_name")
    private String realName;//真实姓名
    @Column(name = "province_code")
    private String provinceCode;//省
    @Column(name = "city_code")
    private String cityCode;//城市
    @Column(name = "area_code")
    private String areaCode;//地区
    @Column(name = "stage_code")
    private String stageCode;//街道
    @Column(name = "grade_code")
    private String gradeCode;//班级
    @Column(name = "school_code")
    private String schoolCode;//学校
    @Column(name = "class_code")
    private String classCode;//班级
    private String version;//版本
    private String extend_;//扩展字段


    @Column(name = "is_band_qq")
    private boolean isBandQQ = false;//是否绑定QQ
    @Column(name = "is_band_wx")
    private boolean isBandWX = false;//是否绑定微信
    @Column(name = "is_band_sina")
    private boolean isBandSina = false;//是否绑定新浪
    @Column(name = "is_band_email")
    private boolean isBandEmail = false;//是否绑定邮箱



    //更新时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateDate = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserPo getUserPo() {
        return userPo;
    }

    public void setUserPo(UserPo userPo) {
        this.userPo = userPo;
    }

    public SourceEnum.EquipmentType getRegisterSource() {
        return registerSource;
    }

    public void setRegisterSource(SourceEnum.EquipmentType registerSource) {
        this.registerSource = registerSource;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

 /*    public UserEnum.BandStatus getBandStatus() {
        return bandStatus;
    }

    public void setBandStatus(UserEnum.BandStatus bandStatus) {
        this.bandStatus = bandStatus;
    }*/

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public UserEnum.Sex getSex() {
        return sex;
    }

    public void setSex(UserEnum.Sex sex) {
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExtend_() {
        return extend_;
    }

    public void setExtend_(String extend_) {
        this.extend_ = extend_;
    }

    public boolean isBandQQ() {
        return isBandQQ;
    }

    public void setBandQQ(boolean isBandQQ) {
        this.isBandQQ = isBandQQ;
    }

    public boolean isBandWX() {
        return isBandWX;
    }

    public void setBandWX(boolean isBandWX) {
        this.isBandWX = isBandWX;
    }

    public boolean isBandSina() {
        return isBandSina;
    }

    public void setBandSina(boolean isBandSina) {
        this.isBandSina = isBandSina;
    }

    public boolean isBandEmail() {
        return isBandEmail;
    }

    public void setBandEmail(boolean isBandEmail) {
        this.isBandEmail = isBandEmail;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public UserEnum.CreateType getCreateType() {
        return createType;
    }

    public void setCreateType(UserEnum.CreateType createType) {
        this.createType = createType;
    }
}
