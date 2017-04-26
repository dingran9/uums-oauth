package com.eeduspace.uuims.oauth.persist.enumeration;


public enum DateTypeEnum {
	 TODAY(0),
	 THREE_DAY(3),
	 SEVEN_DAY(7),
	 ONE_MONTH(30),
	 THREE_MONTH(90),
	 SIX_MONTH(180),
	 ONE_YEAR(365);
	 private final int value;
	 DateTypeEnum(int value){
		this.value=value;
	}
	 public int getValue() {
         return value;
     }
	 
	public static int toEnumValue(String name) {
		for (DateTypeEnum an : DateTypeEnum.values()) {
			if (an.toString().equalsIgnoreCase(name)) {
				return an.getValue();
			}
		}
		return 0;
	}
	 
}
