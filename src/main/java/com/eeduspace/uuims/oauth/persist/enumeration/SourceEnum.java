package com.eeduspace.uuims.oauth.persist.enumeration;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:
 */
public class SourceEnum {

    private EquipmentType equipmentType;
    private EnterpriseType enterpriseType;
    private OnlineSourceType onlineSourceType;
    ;
    public enum EquipmentType  {
        Test (0),
        Web (1),
        Android(2),
        Ios(3),
        Template(4);
        private final int value;

        public int getValue() {
            return value;
        }

        EquipmentType(int value) {
            this.value = value;
        }
    }

    public enum EnterpriseType{
        Tencent (0),
        WeChat (1),
        Sina(2),
        Email(3);
        private final int value;

        public int getValue() {
            return value;
        }

        EnterpriseType(int value) {
            this.value = value;
        }
    }
    
    public enum OnlineSourceType{
    	OnlineSid("online_sid_"),
    	OnlineUid("online_uid_"),
    	OnlinePid("online_pid_"),
    	UnderLine("_");
    	
    	private final String value;
    	
    	public String getValue(){
    		return value;
    	}
    	
		OnlineSourceType(String value) {
			this.value = value;
		}
    	
    }
    

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    public EnterpriseType getEnterpriseType() {
        return enterpriseType;
    }

    public void setEnterpriseType(EnterpriseType enterpriseType) {
        this.enterpriseType = enterpriseType;
    }

	public OnlineSourceType getOnlineSourceType() {
		return onlineSourceType;
	}

	public void setOnlineSourceType(OnlineSourceType onlineSourceType) {
		this.onlineSourceType = onlineSourceType;
	}

}
