package com.eeduspace.uuims.oauth.persist.enumeration;
/**
 * 用户使用情况枚举类
 * @author zhuchaowei
 * 2016年3月21日
 * Description
 */
public class UserUsageCountEnum {
	private CountType countType;
	public enum CountType{
		/**用户注册*/
		USERREGISTER(0),
		/**用户活跃*/
		USERACTIVE(1);
		private final int value;
		CountType(int value){
			this.value=value;
		}
		public int getValue() {
			return value;
		}
	}
	public CountType getCountType() {
		return countType;
	}
	public void setCountType(CountType countType) {
		this.countType = countType;
	}
}
