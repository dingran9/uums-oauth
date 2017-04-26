package com.eeduspace.uuims.oauth.persist.po;

import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: dingran
 * Date: 2015/10/22
 * Description:
 */
@Entity
@Table(name = "auth_manager_log")
public class ManagerLogPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private Long id;

    //管理员ID
    @Column(nullable = false,name = "manager_id")
    private Long managerId;

    //所操纵的资源ID 如：用户、角色、产品 则该处存放用户UUID 以此类推
    @Column(name = "resource_id")
    private String resourceId;

    //产品ID 标识由哪个产品管理员操作
    @Column(name = "product_id")
    private Long productId;

    //发起操作的 设备 （WEB、手机端）
    @Column(name = "source_equipment")
    private SourceEnum.EquipmentType sourceEquipment;

    //资源IP 表示发起操作的 实际IP地址
    @Column(name = "source_ip")
    private String sourceIp;

    //模块 标识操作哪个模块 如：用户、角色、令牌等
    @Column(nullable = false)
    private String module;

    //具体操作动作
    private String action;
    //操作结果
    private Boolean result;

    //描述
    @Column(length = 1000)
    private String description;
    //请求ID
    @Column(name = "request_id")
    private String requestId;
    //创建时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false,name = "create_time")
    private Date createDate = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public SourceEnum.EquipmentType getSourceEquipment() {
        return sourceEquipment;
    }

    public void setSourceEquipment(SourceEnum.EquipmentType sourceEquipment) {
        this.sourceEquipment = sourceEquipment;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
