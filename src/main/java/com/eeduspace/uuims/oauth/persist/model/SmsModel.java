package com.eeduspace.uuims.oauth.persist.model;

import com.eeduspace.uuims.oauth.ActionName;


/**
 * 1.申请  票 
 * 2.获取验证码  返票
 * 3.验证验证码
 * 4.修改
 */
public class SmsModel {

	private String  phone;
	
	private String  ticket;//凭证
	
	private String  code;//验证码
	
	private Integer status;//返回状态
	
	private String password;//密码
	
	private String action;//请求case
	
	private String token;
	
	private String accessKey;//ak
	
	private String secretKey;//sk
	
	
	
	
	

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}


	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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
	
	
	
	
	
	
}
